package me.chriss99;

import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
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
    static int renderProgram;

    static GPUTerrainEroder gpuTerrainEroder;

    static int transformMatrix;
    static InputDeviceManager inputDeviceManager = null;
    static CameraMatrix cameraMatrix = new CameraMatrix();
    static MovementController movementController = null;

    static double deltaTime = 1d/60d;
    static boolean vSync = true;


    static TerrainData terrainData = new TerrainData(HeightMapGenerator.simplexFbm(1000, 1000, 8, 0.0015, 60, 2, .5));
    static final HeightMapTransformer heightMapTransformer = new HeightMapTransformer();
    static boolean simulateErosion = false;


    public static void main(String[] args) {
        glfwInit();
        createWindow();
        gpuTerrainEroder = new GPUTerrainEroder(500, 500);
        double[][][] map = gpuTerrainEroder.downloadMap();
        setupData(map[0], map[1]);
        setupRenderProgram();
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
        final int screenWidth = glfwGetVideoMode(glfwGetPrimaryMonitor()).width();
        final int screenHeight = glfwGetVideoMode(glfwGetPrimaryMonitor()).height();
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

    private static void setupData(double[][] terrainMap, double[][] addedMap) {
        vaoList.add(VAOGenerator.heightMapToSimpleVAO(terrainMap, -100, 100, false));
        vaoList.add(VAOGenerator.heightMapToSimpleVAO(addedMap, -100, 100, true));
        //vaoList.add(VAOGenerator.heightMapToCrossVAO(addedMap, gpuTerrainEroder.downloadWaterOutflow()));
        //vaoList.add(VAOGenerator.heightMapToVectorVAO(terrainData.addedHeights(), terrainData.velocityField));
        //vaoList.add(VAOGenerator.heightMapToNormalVAO(terrainData.terrainMap));
    }

    private static void setupRenderProgram() {
        //load the vertex shader from the file using a method I wrote down below
        vertexShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/shader.vert"), GL_VERTEX_SHADER);

        //load the fragment shader from the file using a method I wrote down below
        fragmentShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/shader.frag"), GL_FRAGMENT_SHADER);

        //create a program object and store its ID in the 'program' variable
        renderProgram = glCreateProgram();

        //these method calls link shader program variables to attribute locations so that they can be modified in Java code
        glBindAttribLocation(renderProgram, 0, "position");
        glBindAttribLocation(renderProgram, 1, "color");

        //attach the vertex and fragment shaders to the program
        glAttachShader(renderProgram, vertexShader);
        glAttachShader(renderProgram, fragmentShader);

        //link the program (whatever that does)
        glLinkProgram(renderProgram);

        //validate the program to make sure it won't blow up the program
        glValidateProgram(renderProgram);

        System.out.println("Stats for render program: ");
        System.out.println("Vertex Shader Compiled: " 		+ glGetShaderi(vertexShader, 	GL_COMPILE_STATUS));
        System.out.println("Fragment Shader Compiled: " 	+ glGetShaderi(fragmentShader, 	GL_COMPILE_STATUS));
        System.out.println("Program Linked: " 				+ glGetProgrami(renderProgram, 		GL_LINK_STATUS));
        System.out.println("Program Validated: " 			+ glGetProgrami(renderProgram, 		GL_VALIDATE_STATUS));
        printErrors();

        //sets the background clear color to white
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //get the 'colorMod and 'positionMod' variables, so I can change them while drawing to create the animation
        transformMatrix = glGetUniformLocation(renderProgram, "transformMatrix");
    }

    private static void loop() {
        double lastTime = glfwGetTime();
        double lastFramePrint = Double.NEGATIVE_INFINITY;
        LinkedList<Double> frames = new LinkedList<>();

        Thread eroder = new Thread();
        while(!glfwWindowShouldClose(window)) {
            glUseProgram(renderProgram);

            movementController.update();
            glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));

            //clear the window
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            /*if (simulateErosion && !eroder.isAlive()) {
                updateTerrainVAOs(terrainData.terrainMap, terrainData.addedHeights());
                eroder = new Thread(() -> {
                    for (int i = 0; i < 5; i++)
                        heightMapTransformer.fullErosion(terrainData);
                });
                eroder.start();
            }*/

            if (simulateErosion) {
                gpuTerrainEroder.erosionStep();
                double[][][] map = gpuTerrainEroder.downloadMap();
                updateTerrainVAOs(map[0], map[1]);
            }

            glUseProgram(renderProgram);

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

    public static void updateTerrainVAOs(double[][] terrainMap, double[][] addedMap) {
        //TODO wont work when the size of the heightMap changes
        vaoList.get(0).updatePositions(VAOGenerator.heightMapToSimpleVertexes(terrainMap, false));
        vaoList.get(0).updateColors(VAOGenerator.heightMapToSimpleColors(terrainMap, -100, 100, false));
        vaoList.get(1).updatePositions(VAOGenerator.heightMapToSimpleVertexes(addedMap, true));
        vaoList.get(1).updateColors(VAOGenerator.heightMapToSimpleColors(addedMap, -100, 100, true));
        //vaoList.get(2).updatePositions(VAOGenerator.heightMapToCrossVertexes(addedMap));
        //vaoList.get(2).updateColors(VAOGenerator.heightMapToCrossColors(addedMap, gpuTerrainEroder.downloadWaterOutflow()));
        //vaoList.get(2).updatePositions(VAOGenerator.heightMapToVectorVertexes(terrainData.addedHeights(), terrainData.velocityField));
        //vaoList.get(2).updatePositions(VAOGenerator.heightMapToNormalVertexes(terrainData.terrainMap));
    }

    private static void cleanGL() {
        glUseProgram(renderProgram);
        //disable the vertex attribute arrays
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        for (VAO vao : vaoList)
            vao.delete();

        //detach the shaders from the program object
        glDetachShader(renderProgram, vertexShader);
        glDetachShader(renderProgram, fragmentShader);

        //delete the shaders now that they are detached
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        glUseProgram(0);
        //delete the program now that the shaders are detached and the program isn't being used
        glDeleteProgram(renderProgram);

        printErrors();


        gpuTerrainEroder.delete();
    }

    public static void updateVSync() {
        glfwSwapInterval(vSync ? 1 : 0);
    }

    public static void printErrors() {
        int error = glGetError();
        while(error != 0) {
            new RuntimeException("OpenGL Error: " + error).printStackTrace();
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