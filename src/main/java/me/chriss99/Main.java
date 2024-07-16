package me.chriss99;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.*;


public class Main {

    //a variable to hold the id of the GLFW window
    static long window;

    static final ArrayList<VAO> vaoList = new ArrayList<>();

    static int vertexShader;
    static int fragmentShader;
    static int renderProgram;

    static int computeShader;
    static int computeProgram;

    static int texture;
    static int textureLocation;
    static int texture2;
    static int texture2Location;

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
        setupData();
        setupComputeProgram();
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

    private static void setupData() {
        vaoList.add(VAOGenerator.heightMapToSimpleVAO(terrainData.terrainMap, -100, 100, false));
        vaoList.add(VAOGenerator.heightMapToSimpleVAO(terrainData.addedHeights(), -100, 100, true));
        //vaoList.add(VAOGenerator.heightMapToVectorVAO(terrainData.addedHeights(), terrainData.velocityField));
        //vaoList.add(VAOGenerator.heightMapToNormalVAO(terrainData.terrainMap));

        ByteBuffer imageData = BufferUtils.createByteBuffer(2*2*4*4);
        imageData.putFloat(1);
        imageData.position(4*4);
        imageData.putFloat(1);
        imageData.position(8*4);
        imageData.putFloat(1);
        imageData.position(12*4);
        imageData.putFloat(1);
        imageData.position(0);

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA16F, 2, 2);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 2, 2, GL_RGBA, GL_FLOAT, imageData);

        texture2 = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture2);
        glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA16F, 2, 2);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 2, 2, GL_RGBA, GL_FLOAT, imageData);
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

        //check for compilation errors
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

    private static void setupComputeProgram() {
        computeShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/shader.comp"), GL_COMPUTE_SHADER);

        //create a program object and store its ID in the 'program' variable
        computeProgram = glCreateProgram();

        glAttachShader(computeProgram, computeShader);

        //link the program (whatever that does)
        glLinkProgram(computeProgram);

        //validate the program to make sure it won't blow up the program
        glValidateProgram(computeProgram);

        //check for compilation errors
        System.out.println("Compute Shader Compiled: "   	+ glGetShaderi(computeShader, 	GL_COMPILE_STATUS));
        System.out.println("Program Linked: " 				+ glGetProgrami(computeProgram, 		GL_LINK_STATUS));
        System.out.println("Program Validated: " 			+ glGetProgrami(computeProgram, 		GL_VALIDATE_STATUS));
        printErrors();

        glUseProgram(computeProgram);

        glBindImageTexture(1, texture, 0, false, 0, GL_READ_WRITE, GL_RGBA16F);
        textureLocation = glGetUniformLocation(computeProgram, "myImage");
        glUniform1i(textureLocation, 1);

        glBindImageTexture(2, texture2, 0, false, 0, GL_READ_WRITE, GL_RGBA16F);
        texture2Location = glGetUniformLocation(computeProgram, "myImage2");
        glUniform1i(texture2Location, 2);
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

            if (simulateErosion && !eroder.isAlive()) {
                updateTerrainVAOs();
                eroder = new Thread(() -> {
                    for (int i = 0; i < 5; i++)
                        heightMapTransformer.fullErosion(terrainData);
                });
                eroder.start();
            }

            for (VAO vao : vaoList) {
                vao.bind();

                //draw the current bound VAO/VBO using an index buffer
                glDrawElements(GL_TRIANGLES, vao.indexLength(), GL_UNSIGNED_INT, 0);
            }

            //swap the frame to show the rendered image
            glfwSwapBuffers(window);



            glUseProgram(computeProgram);
            glDispatchCompute(2, 2, 1);
            glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(16*4);

            glBindTexture(GL_TEXTURE_2D, texture);
            glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_FLOAT, byteBuffer);
            for (int i = 0; i < 16; i++)
                System.out.print(byteBuffer.getFloat(i*4) + ", ");

            glBindTexture(GL_TEXTURE_2D, texture2);
            glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_FLOAT, byteBuffer);
            for (int i = 0; i < 16; i++)
                System.out.print(byteBuffer.getFloat(i*4) + ", ");

            System.out.println();



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
        vaoList.get(0).updatePositions(VAOGenerator.heightMapToSimpleVertexes(terrainData.terrainMap, false));
        vaoList.get(0).updateColors(VAOGenerator.heightMapToSimpleColors(terrainData.terrainMap, -100, 100, false));
        vaoList.get(1).updatePositions(VAOGenerator.heightMapToSimpleVertexes(terrainData.addedHeights(), true));
        vaoList.get(1).updateColors(VAOGenerator.heightMapToSimpleColors(terrainData.addedHeights(), -100, 100, true));
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



        glUseProgram(computeProgram);

        glDetachShader(computeProgram, computeShader);

        glDeleteShader(computeShader);



        glUseProgram(0);
        //delete the program now that the shaders are detached and the program isn't being used
        glDeleteProgram(renderProgram);
        glDeleteProgram(computeProgram);

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