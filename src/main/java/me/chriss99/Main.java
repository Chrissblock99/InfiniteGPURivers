package me.chriss99;

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
    static int renderTransformMatrix;

    static int passThroughShader;
    static int tessControlShader;
    static int tessEvaluationShader;
    static int gradientShader;
    static int tesselationProgram;
    static int tessTransformMatrix;

    static GPUTerrainEroder gpuTerrainEroder;
    static int vao;
    static int vertexes;
    static boolean simulateErosion = false;

    static InputDeviceManager inputDeviceManager = null;
    static CameraMatrix cameraMatrix = new CameraMatrix();
    static MovementController movementController = null;

    static double deltaTime = 1d/60d;
    static boolean vSync = true;


    public static void main(String[] args) {
        glfwInit();
        createWindow();
        gpuTerrainEroder = new GPUTerrainEroder(500, 500);
        setupData();
        setupRenderProgram();
        setupTesselationProgram();
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
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        ByteBuffer vertices = Util.storeArrayInBuffer(VAOGenerator.tesselationGridVertexesTest(5, 5, 100));

        vertexes = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexes);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_DOUBLE, false, 0, 0);
        glEnableVertexAttribArray(0);
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
        renderTransformMatrix = glGetUniformLocation(renderProgram, "transformMatrix");
    }

    private static void setupTesselationProgram() {
        glPatchParameteri(GL_PATCH_VERTICES, 4);

        passThroughShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/passThrough.vert"), GL_VERTEX_SHADER);
        tessControlShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/tess.tesc"), GL_TESS_CONTROL_SHADER);
        tessEvaluationShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/tess.tese"), GL_TESS_EVALUATION_SHADER);
        gradientShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/gradient.frag"), GL_FRAGMENT_SHADER);

        //create a program object and store its ID in the 'program' variable
        tesselationProgram = glCreateProgram();

        //these method calls link shader program variables to attribute locations so that they can be modified in Java code
        glBindAttribLocation(tesselationProgram, 0, "position");

        //attach the vertex and fragment shaders to the program
        glAttachShader(tesselationProgram, passThroughShader);
        glAttachShader(tesselationProgram, tessControlShader);
        glAttachShader(tesselationProgram, tessEvaluationShader);
        glAttachShader(tesselationProgram, gradientShader);

        //link the program (whatever that does)
        glLinkProgram(tesselationProgram);

        //validate the program to make sure it won't blow up the program
        glValidateProgram(tesselationProgram);

        System.out.println("Stats for render program: ");
        System.out.println("Vertex Shader Compiled: " 		+ glGetShaderi(passThroughShader, 	    GL_COMPILE_STATUS));
        System.out.println("Control Shader Compiled: " 		+ glGetShaderi(tessControlShader, 	    GL_COMPILE_STATUS));
        System.out.println("Evaluation Shader Compiled: " 	+ glGetShaderi(tessEvaluationShader, 	GL_COMPILE_STATUS));
        System.out.println("Fragment Shader Compiled: " 	+ glGetShaderi(gradientShader, 	        GL_COMPILE_STATUS));
        System.out.println("Program Linked: " 				+ glGetProgrami(tesselationProgram, 	GL_LINK_STATUS));
        System.out.println("Program Validated: " 			+ glGetProgrami(tesselationProgram, 	GL_VALIDATE_STATUS));
        printErrors();

        //sets the background clear color to white
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //get the 'colorMod and 'positionMod' variables, so I can change them while drawing to create the animation
        tessTransformMatrix = glGetUniformLocation(tesselationProgram, "transformMatrix");
    }

    private static void loop() {
        double lastTime = glfwGetTime();
        double lastFramePrint = Double.NEGATIVE_INFINITY;
        LinkedList<Double> frames = new LinkedList<>();

        while(!glfwWindowShouldClose(window)) {
            movementController.update();

            //clear the window
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (simulateErosion) {
                for (int i = 0; i < 100; i++)
                    gpuTerrainEroder.erosionStep();
            }

            glUseProgram(tesselationProgram);
            glUniformMatrix4fv(tessTransformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
            glBindVertexArray(vao);
            glDrawArrays(GL_PATCHES, 0, 5*5*8);

            glUseProgram(renderProgram);
            glUniformMatrix4fv(renderTransformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));

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

    private static void cleanGL() {
        //disable the vertex attribute arrays
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        for (VAO vao : vaoList)
            vao.delete();

        glUseProgram(renderProgram);
        //detach the shaders from the program object
        glDetachShader(renderProgram, vertexShader);
        glDetachShader(renderProgram, fragmentShader);

        //delete the shaders now that they are detached
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        glUseProgram(0);
        //delete the program now that the shaders are detached and the program isn't being used
        glDeleteProgram(renderProgram);


        glUseProgram(tesselationProgram);
        //detach the shaders from the program object
        glDetachShader(tesselationProgram, passThroughShader);
        glDetachShader(tesselationProgram, tessControlShader);
        glDetachShader(tesselationProgram, tessEvaluationShader);
        glDetachShader(tesselationProgram, gradientShader);

        //delete the shaders now that they are detached
        glDeleteShader(passThroughShader);
        glDeleteShader(tessControlShader);
        glDeleteShader(tessEvaluationShader);
        glDeleteShader(gradientShader);

        glUseProgram(0);
        //delete the program now that the shaders are detached and the program isn't being used
        glDeleteProgram(tesselationProgram);

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