package me.chriss99;

import me.chriss99.program.*;
import me.chriss99.worldmanagement.InfiniteWorld;
import org.joml.Vector2i;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.util.*;


public class Main {
    static long window;

    static boolean niceRender = true;
    static RenderProgram vaoListProgram;
    static PlayerCenteredRenderer playerCenteredRenderer;
    static RenderProgram tessProgram;
    static RenderProgram niceTessProgram;

    static ErosionDataStorage worldStorage;

    static Vector2i srcPos = new Vector2i(-7*64, 5*64);
    static int xSize = 8*64;
    static int zSize = 8*64;
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
        double start = glfwGetTime();
        createWindow();
        worldStorage = new ErosionDataStorage("testT", TerrainGenerator::generateChunk);
        gpuTerrainEroder = new GPUTerrainEroder(worldStorage, srcPos, xSize+1, zSize+1);

        vaoListProgram = new VAOListProgram(cameraMatrix, List.of(/*VAOGenerator.heightMapToSimpleVAO(new double[][]{{0d, 0d, 0d}, {0d, 1d, 0d}, {0d, 0d, 0d}}, -1, 2, true)*/)); //test case for rendering
        playerCenteredRenderer = new PlayerCenteredRenderer(cameraMatrix, vector2i -> {
            Float2DBufferWrapper terrain = worldStorage.terrain.readArea(vector2i.x, vector2i.y, 65, 65).asFloatWrapper();
            Float2DBufferWrapper water = worldStorage.water.readArea(vector2i.x, vector2i.y, 65, 65).asFloatWrapper();

            return TerrainVAOGenerator.heightMapToSimpleVAO(terrain, water, vector2i);
        }, 7, srcPos, new Vector2i(xSize, zSize));

        setupData();

        glPatchParameteri(GL_PATCH_VERTICES, 4);
        tessProgram = new TessProgram(cameraMatrix, vao, srcPos, xSize, zSize, false);
        niceTessProgram = new TessProgram(cameraMatrix, vao, srcPos, xSize, zSize, true);

        inputDeviceManager = new InputDeviceManager(window);
        movementController = new MovementController(inputDeviceManager, cameraMatrix);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        GLUtil.setupDebugMessageCallback();
        updateVSync();

        System.out.println("Started after: " + (glfwGetTime() - start));
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

        ByteBuffer vertices = Util.storeArrayInBuffer(VAOGenerator.tesselationGridVertexesTest(xSize/64, zSize/64, 64));

        vertexes = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexes);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_DOUBLE, false, 0, 0);
        glEnableVertexAttribArray(0);
    }

    private static void loop() {
        double lastTime = glfwGetTime();
        double lastFramePrint = Double.NEGATIVE_INFINITY;
        LinkedList<Double> frames = new LinkedList<>();

        while(!glfwWindowShouldClose(window)) {
            movementController.update();
            playerCenteredRenderer.updateLoadedChunks(srcPos, new Vector2i(xSize, zSize));

            //clear the window
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            vaoListProgram.render();
            //terrainVAOListProgram.render();
            //terrainVAOListProgram.render(true);
            playerCenteredRenderer.render();
            if (niceRender)
                niceTessProgram.render();
            else
                tessProgram.render();

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

        gpuTerrainEroder.downloadMap();
        worldStorage.unloadAll();
    }

    private static void cleanGL() {
        //disable the vertex attribute arrays
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        vaoListProgram.delete();
        tessProgram.delete();
        niceTessProgram.delete();

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