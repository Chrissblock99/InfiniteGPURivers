package me.chriss99;

import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class Main {

    //a variable to hold the id of the GLFW window
    static long window;

    static final ArrayList<VAO> vaoList = new ArrayList<>();

    static int vertexShader;
    static int fragmentShader;
    static int program;

    static int transformMatrix;
    static InputDeviceManager inputDeviceManager = null;
    static CameraMatrix cameraMatrix = new CameraMatrix();
    static MovementController movementController = null;

    static double deltaTime = 1d/60d;
    static boolean vSync = true;


    static TerrainData terrainData = new TerrainData(VAOGenerator.pillar(100, 100));
    static final HeightMapTransformer heightMapTransformer = new HeightMapTransformer();
    static boolean simulateThermal = false;
    static boolean simulateHydraulic = false;


    public static void main(String[] args) {
        glfwInit();
        createWindow();
        setupData();
        setupProgram();
        inputDeviceManager = new InputDeviceManager(window);
        movementController = new MovementController(inputDeviceManager, cameraMatrix);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        updateVSync();

        loop();
        System.out.println("Window closed");

        cleanGL();
    }

    private static void createWindow() {
        final int screenWidth = glfwGetVideoMode(glfwGetPrimaryMonitor()).width()/2;
        final int screenHeight = glfwGetVideoMode(glfwGetPrimaryMonitor()).height()/2;
        cameraMatrix.aspectRatio = (float) screenHeight / (float) screenWidth;

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        //create a GLFW window and store its id in the window variable
        window = glfwCreateWindow(screenWidth, screenHeight, "GLFW OpenGL Window", NULL, 0);

        //enables opengl
        glfwMakeContextCurrent(window);

        //create GLCapabilities instance because it's required (stupid, I know) and use it to print out if OpenGL 4.5 is supported
        System.out.println("OpenGL 4.5 Supported: " + GL.createCapabilities().OpenGL45);

        //make the opengl screen 1600 pixels wide and 900 pixels tall.
        glViewport(0, 0, screenWidth, screenHeight);

        //show the window
        glfwShowWindow(window);
    }

    private static void setupData() {
        double[] triangle = {
                0, 0, 0,
                1, 0, 0,
                0, 1, 0,
                1, 1, 0,
                0, 0, 1,
                1, 0, 1,
                0, 1, 1,
                1, 1, 1,
        };

        //the color data (red, green, and blue)
        double[] color = {
                1, 0, 0,
                0, 0, 1,
                0, 0, 1,
                0, 1, 0,
                1, 0, 0,
                0, 0, 1,
                0, 1, 0,
                1, 0, 0,
        };

        //the order to render the vertices
        int[] index = new int[] {
                0,
                1,
                2,
                3,
                6,
                7,
                4,
                5,
                5, //hack
                2, //hack
                2,
                6,
                0,
                4,
                1,
                5,
                3,
                7,
        };

        int cubesNumberX = 0;
        int cubesNumberZ = 0;

        for (int x = 0; x < cubesNumberX; x++) {
            for (int z = 0; z < cubesNumberZ; z++) {
                double[] triangleCopy = Arrays.copyOf(triangle, triangle.length);
                for (int i = 0; i < 8; i++) {
                    triangleCopy[i * 3] += 2 * x;
                    triangleCopy[i * 3 + 2] += 2 * z;
                }
                vaoList.add(new VAO(triangleCopy, color, index));
            }
        }

        vaoList.add(VAOGenerator.heightMapToSimpleVAO(terrainData.terrainMap));
        vaoList.add(VAOGenerator.heightMapToCrossVAO(terrainData.addedHeights(), terrainData.waterOutFlowPipes));

        //for (int x = 0; x < terrainData.xSize; x++)
        //    System.out.println(Arrays.toString(terrainData.waterMap[x]));

    }

    private static void setupProgram() {
        //load the vertex shader from the file using a method I wrote down below
        vertexShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/shader.vert"), GL_VERTEX_SHADER);

        //load the fragment shader from the file using a method I wrote down below
        fragmentShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/shader.frag"), GL_FRAGMENT_SHADER);

        //create a program object and store its ID in the 'program' variable
        program = glCreateProgram();

        //these method calls link shader program variables to attribute locations so that they can be modified in Java code
        glBindAttribLocation(program, 0, "position");
        glBindAttribLocation(program, 1, "color");

        //attach the vertex and fragment shaders to the program
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        //link the program (whatever that does)
        glLinkProgram(program);

        //validate the program to make sure it wont blow up the program
        glValidateProgram(program);

        //check for compilation errors
        System.out.println("Vertex Shader Compiled: " 		+ glGetShaderi(vertexShader, 	GL_COMPILE_STATUS));
        System.out.println("Fragment Shader Compiled: " 	+ glGetShaderi(fragmentShader, 	GL_COMPILE_STATUS));
        System.out.println("Program Linked: " 				+ glGetProgrami(program, 		GL_LINK_STATUS));
        System.out.println("Program Validated: " 			+ glGetProgrami(program, 		GL_VALIDATE_STATUS));
        printErrors();

        //sets the background clear color to white
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //get the 'colorMod and 'positionMod' variables so I can change them while drawing to create the animation
        transformMatrix = glGetUniformLocation(program, "transformMatrix");

        //set the current program
        glUseProgram(program);
    }

    private static void loop() {
        double lastTime = glfwGetTime();
        double lastFramePrint = Double.NEGATIVE_INFINITY;
        LinkedList<Double> frames = new LinkedList<>();

        while(!glfwWindowShouldClose(window)) {
            movementController.update();
            glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));

            //clear the window
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (simulateThermal) heightMapTransformer.simpleThermalErosion(terrainData.terrainMap);
            if (simulateHydraulic) heightMapTransformer.simpleHydraulicErosion(terrainData);
            if (simulateThermal || simulateHydraulic) updateTerrainVAOs();

            for (VAO vao : vaoList) {
                vao.bind();

                //draw the current bound VAO/VBO using an index buffer
                glDrawElements(GL_TRIANGLES, vao.indexLength(), GL_UNSIGNED_INT, 0);
            }

            //swap the frame to show the rendered image
            glfwSwapBuffers(window);

            //poll for window events (resize, close, button presses, etc.)
            glfwPollEvents();


            double currentTime = glfwGetTime();
            frames.add(currentTime);
            Iterator<Double> iterator = frames.iterator();
            for (int i = 0; i < frames.size(); i++)
                if (currentTime - iterator.next() >= 1)
                    iterator.remove();
                else break;

            deltaTime = currentTime - lastTime;
            if (!vSync && currentTime - lastFramePrint > .5) {
                System.out.println(frames.size() + "   " + Math.round(1/deltaTime) + "   " + deltaTime*1000);
                lastFramePrint = currentTime;
            }
            lastTime = currentTime;
        }
    }

    public static void updateTerrainVAOs() {
        //TODO wont work when the size of the heightMap changes
        vaoList.get(0).updatePositions(VAOGenerator.heightMapToSimpleVertexes(terrainData.terrainMap));
        vaoList.get(0).updateColors(VAOGenerator.heightMapToSimpleColors(terrainData.terrainMap));
        vaoList.get(1).updatePositions(VAOGenerator.heightMapToCrossVertexes(terrainData.addedHeights()));
        vaoList.get(1).updateColors(VAOGenerator.heightMapToCrossColors(terrainData.addedHeights(), terrainData.waterOutFlowPipes));
    }

    private static void cleanGL() {
        //disable the vertex attribute arrays
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        for (VAO vao : vaoList)
            vao.delete();

        //detach the shaders from the program object
        glDetachShader(program, vertexShader);
        glDetachShader(program, fragmentShader);

        //delete the shaders now that they are deatched
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        //stop using the shader program
        glUseProgram(0);

        //delete the program now that the shaders are detached and the program isn't being used
        glDeleteProgram(program);

        printErrors();
    }

    public static void updateVSync() {
        glfwSwapInterval(vSync ? 1 : 0);
    }

    private static void printErrors() {
        int error = glGetError();
        while(error != 0) {
            System.out.println("OpenGL Error: " + error);
            error = glGetError();
        }
    }

    public static int loadShader(File file, int type) {
        try {
            Scanner sc = new Scanner(file);
            StringBuilder data = new StringBuilder();

            if(file.exists()) {
                while(sc.hasNextLine()) {
                    data.append(sc.nextLine()).append("\n");
                }

                sc.close();
            }
            int id = glCreateShader(type);
            glShaderSource(id, data);
            glCompileShader(id);
            return id;
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }
}