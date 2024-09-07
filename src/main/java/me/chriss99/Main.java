package me.chriss99;

import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.util.*;


public class Main {

    //a variable to hold the id of the GLFW window
    static long window;

    static final ArrayList<VAO> vaoList = new ArrayList<>();

    static GLProgram renderProgram;
    static int renderTransformMatrix;

    static GLProgram tessProgram;
    static int tessTransformMatrix;
    static int tessWaterUniform;

    static GLProgram niceTessProgram;
    static int niceTessTransformMatrix;
    static int niceTessWaterUniform;

    static int xSize = 500;
    static int zSize = 500;
    static int simulationStepsPerFrame = 5;

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
        gpuTerrainEroder = new GPUTerrainEroder(xSize, zSize);
        setupData();

        setupRenderProgram();
        setupTesselationProgram();
        setupNiceTesselationProgram();

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

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //show the window
        glfwShowWindow(window);
    }

    private static void setupData() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        ByteBuffer vertices = Util.storeArrayInBuffer(VAOGenerator.tesselationGridVertexesTest(xSize/100, zSize/100, 100));

        vertexes = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexes);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_DOUBLE, false, 0, 0);
        glEnableVertexAttribArray(0);
    }

    private static void setupRenderProgram() {
        renderProgram = new GLProgram();

        renderProgram.addShader("shader.vert", GL_VERTEX_SHADER);
        renderProgram.addShader("shader.frag", GL_FRAGMENT_SHADER);

        renderProgram.bindAttribute(0, "position");
        renderProgram.bindAttribute(1, "color");

        renderProgram.validate();

        renderTransformMatrix = renderProgram.getUniform("transformMatrix");
    }

    private static void setupTesselationProgram() {
        glPatchParameteri(GL_PATCH_VERTICES, 4);

        tessProgram = new GLProgram();

        tessProgram.addShader("passThrough.vert", GL_VERTEX_SHADER);
        tessProgram.addShader("tess.tesc", GL_TESS_CONTROL_SHADER);
        tessProgram.addShader("tess.tese", GL_TESS_EVALUATION_SHADER);
        tessProgram.addShader("gradient.frag", GL_FRAGMENT_SHADER);

        tessProgram.bindAttribute(0, "position");

        tessProgram.validate();

        tessTransformMatrix = tessProgram.getUniform("transformMatrix");
        tessWaterUniform = tessProgram.getUniform("water");
    }

    private static void setupNiceTesselationProgram() {
        glPatchParameteri(GL_PATCH_VERTICES, 4);

        niceTessProgram = new GLProgram();

        niceTessProgram.addShader("passThrough.vert", GL_VERTEX_SHADER);
        niceTessProgram.addShader("tess.tesc", GL_TESS_CONTROL_SHADER);
        niceTessProgram.addShader("niceTess.tese", GL_TESS_EVALUATION_SHADER);
        niceTessProgram.addShader("normals.geom", GL_GEOMETRY_SHADER);
        niceTessProgram.addShader("different.frag", GL_FRAGMENT_SHADER);

        niceTessProgram.bindAttribute(0, "position");

        niceTessProgram.validate();

        niceTessTransformMatrix = niceTessProgram.getUniform("transformMatrix");
        niceTessWaterUniform = niceTessProgram.getUniform("water");
    }

    private static void loop() {
        double lastTime = glfwGetTime();
        double lastFramePrint = Double.NEGATIVE_INFINITY;
        LinkedList<Double> frames = new LinkedList<>();

        while(!glfwWindowShouldClose(window)) {
            movementController.update();

            //clear the window
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            niceTessProgram.use();
            glUniformMatrix4fv(tessTransformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
            glUniformMatrix4fv(niceTessTransformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
            glBindVertexArray(vao);

            glUniform1i(tessWaterUniform, 0);
            glUniform1i(niceTessWaterUniform, 0);
            glDrawArrays(GL_PATCHES, 0, xSize/100*zSize/100*4);
            glUniform1i(tessWaterUniform, 1);
            glUniform1i(niceTessWaterUniform, 1);
            glDrawArrays(GL_PATCHES, 0, xSize/100*zSize/100*4);

            renderProgram.use();
            glUniformMatrix4fv(renderTransformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));

            for (VAO vao : vaoList) {
                vao.bind();

                //draw the current bound VAO/VBO using an index buffer
                glDrawElements(GL_TRIANGLES, vao.indexLength(), GL_UNSIGNED_INT, 0);
            }

            //swap the frame to show the rendered image
            glfwSwapBuffers(window);

            if (simulateErosion)
                gpuTerrainEroder.erosionSteps(simulationStepsPerFrame);

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

        renderProgram.delete();
        tessProgram.delete();

        gpuTerrainEroder.delete();
        printErrors();
    }

    public static void updateVSync() {
        glfwSwapInterval(vSync ? 1 : 0);
    }

    public static void printErrors() {
        int error = glGetError();
        while(error != 0) {
            new RuntimeException("OpenGL Error: " + glGetErrorString(error)).printStackTrace();
            error = glGetError();
        }
    }

    public static String glGetErrorString(int error) {
        return switch (error) {
            case GL_NO_ERROR -> "No Error";
            case GL_INVALID_ENUM -> "Invalid Enum";
            case GL_INVALID_VALUE -> "Invalid Value";
            case GL_INVALID_OPERATION -> "Invalid Operation";
            case GL_INVALID_FRAMEBUFFER_OPERATION -> "Invalid Framebuffer Operation";
            case GL_OUT_OF_MEMORY -> "Out of Memory";
            case GL_STACK_UNDERFLOW -> "Stack Underflow";
            case GL_STACK_OVERFLOW -> "Stack Overflow";
            case GL_CONTEXT_LOST -> "Context Lost";
            default -> "Unknown Error (" + error + ")";
        };
    }
}