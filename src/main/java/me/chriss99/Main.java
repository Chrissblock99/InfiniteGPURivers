package me.chriss99;

import me.chriss99.program.*;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.util.*;


public class Main {
    static long window;

    static ListRenderer<ColoredVAO> vaoListProgram;
    static PositionCenteredRenderer<TerrainVAO> playerCenteredRenderer;
    static boolean renderIterations = false;
    static PositionCenteredRenderer<IterationVAO> iterationRenderer;
    static TessProgram tessProgram;

    static ErosionDataStorage worldStorage;

    //config -----------------
    static final String worldName = "test64";
    static final int chunkSize = 64;
    static final int regionSize = 10;
    static final int iterationChunkSize = 64;
    static final int iterationRegionSize = 10;

    static final int chunkRenderDistance = 7;
    static final int iterationRenderDistance = 2;

    static final Vector2i srcPos = new Vector2i(-7*64, 5*64);
    static final int xSize = 8*64;
    static final int zSize = 8*64;

    static int simulationStepsPerFrame = 5;
    //config -----------------

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
        worldStorage = new ErosionDataStorage(worldName, chunkSize, regionSize, iterationChunkSize, iterationRegionSize);
        gpuTerrainEroder = new GPUTerrainEroder(worldStorage, srcPos, xSize+1, zSize+1);

        vaoListProgram = new ListRenderer<>(new ColoredVAORenderer(cameraMatrix), List.of(/*ColoredVAOGenerator.heightMapToSimpleVAO(new double[][]{{0d, 0d, 0d}, {0d, 1d, 0d}, {0d, 0d, 0d}}, -1, 2, true)*/)); //test case for rendering
        playerCenteredRenderer = new PositionCenteredRenderer<>(new TerrainVAORenderer(cameraMatrix), (vector2i, chunkSize) -> {
            chunkSize++;
            Float2DBufferWrapper terrain = worldStorage.terrain.readArea(vector2i.x, vector2i.y, chunkSize, chunkSize).asFloatWrapper();
            Float2DBufferWrapper water = worldStorage.water.readArea(vector2i.x, vector2i.y, chunkSize, chunkSize).asFloatWrapper();

            return TerrainVAOGenerator.heightMapToSimpleVAO(terrain, water, vector2i);
        }, cameraMatrix.position, worldStorage.chunkSize, chunkRenderDistance, srcPos, new Vector2i(xSize, zSize));
        iterationRenderer = new PositionCenteredRenderer<>(new IterationVAORenderer(cameraMatrix),
                (vector2i, chunkSize) -> IterationVAOGenerator.heightMapToIterationVAO(vector2i, new Vector2i(chunkSize), worldStorage.iterationInfo),
                cameraMatrix.position, worldStorage.iterationChunkSize, iterationRenderDistance);

        setupData();

        glPatchParameteri(GL_PATCH_VERTICES, 4);
        tessProgram = new TessProgram(cameraMatrix, vao, srcPos, xSize, zSize);

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

        ByteBuffer vertices = Util.storeArrayInBuffer(ColoredVAOGenerator.tesselationGridVertexesTest(xSize/64, zSize/64, 64));

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
            playerCenteredRenderer.updateLoadedChunks(cameraMatrix.position, srcPos, new Vector2i(xSize, zSize));
            if (renderIterations)
                iterationRenderer.updateLoadedChunks(new Vector3f(cameraMatrix.position).div(64));

            //clear the window
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            vaoListProgram.render();
            tessProgram.renderTerrain();
            playerCenteredRenderer.render();
            tessProgram.renderWater();
            if (renderIterations)
                iterationRenderer.render();

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
        System.out.println("Saving world...");
        worldStorage.unloadAll();
        System.out.println("Saved world in " + (glfwGetTime() - lastTime) + " seconds.");
    }

    private static void cleanGL() {
        //disable the vertex attribute arrays
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        vaoListProgram.delete();
        playerCenteredRenderer.delete();
        iterationRenderer.delete();
        tessProgram.delete();

        worldStorage.cleanGL();
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