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
    private long window;

    public final  ListRenderer<ColoredVAO> vaoListProgram;
    public final  PositionCenteredRenderer<TerrainVAO> playerCenteredRenderer;
    public boolean renderIterations = false;
    public final  PositionCenteredRenderer<IterationVAO> iterationRenderer;
    public final TessProgram tessProgram;

    public final ErosionDataStorage worldStorage;
    public final ErosionManager erosionManager;

    public boolean wireFrame = false;

    public final GPUTerrainEroder gpuTerrainEroder;
    private int vao;
    private int vertexes;
    public boolean simulateErosion = false;
    public int simulationStepsPerFrame;

    public final InputDeviceManager inputDeviceManager;
    public final CameraMatrix cameraMatrix = new CameraMatrix();
    public final InputController inputController;

    private double deltaTime = 1d/60d;
    public boolean vSync = true;


    public static void main(String[] args) {
        glfwInit();
        double start = glfwGetTime();

        Main main = new Main("test64",
                64, 10, 64, 10,
                7, 2,
                new Vector2i(), new Vector2i(8*64),
                5);

        System.out.println("Started after: " + (glfwGetTime() - start));
        main.loop();
        System.out.println("Window closed");

        main.cleanGL();
    }

    public Main(String worldName,
                int chunkSize, int regionSize, int iterationChunkSize, int iterationRegionSize,
                int chunkRenderDistance, int iterationRenderDistance,
                Vector2i srcPos, Vector2i size,
                int simulationStepsPerFrame) {
        createWindow();
        worldStorage = new ErosionDataStorage(worldName, chunkSize, regionSize, iterationChunkSize, iterationRegionSize);
        gpuTerrainEroder = new GPUTerrainEroder(worldStorage, srcPos, new Vector2i(size).add(1, 1), new Vector2i(size).add(1, 1));
        erosionManager = new ErosionManager(gpuTerrainEroder, worldStorage.iterationInfo);
        this.simulationStepsPerFrame = simulationStepsPerFrame;

        vaoListProgram = new ListRenderer<>(new ColoredVAORenderer(cameraMatrix), List.of(/*ColoredVAOGenerator.heightMapToSimpleVAO(new double[][]{{0d, 0d, 0d}, {0d, 1d, 0d}, {0d, 0d, 0d}}, -1, 2, true)*/)); //test case for rendering
        playerCenteredRenderer = new PositionCenteredRenderer<>(new TerrainVAORenderer(cameraMatrix), (srcPos1, chunkSize1) -> {
            chunkSize1++;
            Float2DBufferWrapper terrain = (Float2DBufferWrapper) worldStorage.terrain.readArea(srcPos1.x, srcPos1.y, chunkSize1, chunkSize1);
            Float2DBufferWrapper water = (Float2DBufferWrapper) worldStorage.water.readArea(srcPos1.x, srcPos1.y, chunkSize1, chunkSize1);

            return TerrainVAOGenerator.heightMapToSimpleVAO(terrain, water, srcPos1, 1);
        }, cameraMatrix.position, worldStorage.chunkSize, chunkRenderDistance, srcPos, new Vector2i(size));
        iterationRenderer = new PositionCenteredRenderer<>(new IterationVAORenderer(cameraMatrix),
                (vector2i, chunkSize1) -> IterationVAOGenerator.heightMapToIterationVAO(vector2i, new Vector2i(chunkSize1), worldStorage.iterationInfo),
                cameraMatrix.position, worldStorage.iterationChunkSize, iterationRenderDistance);

        setupData(size);

        glPatchParameteri(GL_PATCH_VERTICES, 4);
        tessProgram = new TessProgram(cameraMatrix, vao, srcPos, size.x, size.y);

        inputDeviceManager = new InputDeviceManager(window);
        inputController = new InputController(inputDeviceManager, this);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glEnable(GL_DEPTH_TEST);
        GLUtil.setupDebugMessageCallback();
        updateWireFrame();
        updateVSync();
    }

    public void primitiveErosion() {
        Vector2i pos = new Vector2i(Util.properIntDivide((int) cameraMatrix.position.x, worldStorage.chunkSize), Util.properIntDivide((int) cameraMatrix.position.z, worldStorage.chunkSize));
        if (erosionManager.findIterate(pos, 10)) {
            tessProgram.setSrcPos(gpuTerrainEroder.getSrcPos());

            iterationRenderer.reloadAll();
        } else System.out.println("Nah...");
    }

    private void createWindow() {
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

    private void setupData(Vector2i maxSize) {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        ByteBuffer vertices = Util.storeArrayInBuffer(ColoredVAOGenerator.tesselationGridVertexesTest(maxSize.x/64, maxSize.y/64, 64));

        vertexes = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexes);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_DOUBLE, false, 0, 0);
        glEnableVertexAttribArray(0);
    }

    private void loop() {
        double lastTime = glfwGetTime();
        double lastFramePrint = Double.NEGATIVE_INFINITY;
        LinkedList<Double> frames = new LinkedList<>();
        double lastIPSPrint = Double.NEGATIVE_INFINITY;
        LinkedList<Double> iteratedFrames = new LinkedList<>();

        while(!glfwWindowShouldClose(window)) {
            inputController.update(deltaTime);
            playerCenteredRenderer.updateLoadedChunks(cameraMatrix.position, gpuTerrainEroder.getSrcPos(), gpuTerrainEroder.getSize());
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
                primitiveErosion();
                //gpuTerrainEroder.erosionSteps(simulationStepsPerFrame, true, true, true, true);

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

            if (simulateErosion) {
                iteratedFrames.add(currentTime);
                Iterator<Double> iterator2 = iteratedFrames.iterator();
                for (int i = 0; i < iteratedFrames.size(); i++)
                    if (currentTime - iterator2.next() >= 1)
                        iterator2.remove();
                    else break;
            }

            if (simulateErosion && currentTime - lastIPSPrint > 1) {
                Vector2i size = gpuTerrainEroder.getSize();
                System.out.println("ips/1b: " + ((float) size.x * size.y) * ((float) simulationStepsPerFrame * iteratedFrames.size()) / ((currentTime - lastIPSPrint) * 1000000000f));
                lastIPSPrint = currentTime;
            }

            lastTime = currentTime;
        }

        gpuTerrainEroder.downloadMap();
        System.out.println("Saving world...");
        worldStorage.unloadAll();
        System.out.println("Saved world in " + (glfwGetTime() - lastTime) + " seconds.");
    }

    private void cleanGL() {
        //disable the vertex attribute arrays
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        vaoListProgram.delete();
        playerCenteredRenderer.delete();
        iterationRenderer.delete();
        tessProgram.delete();

        worldStorage.cleanGL();
        gpuTerrainEroder.delete();
    }

    public void updateWireFrame() {
        if (wireFrame) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glDisable(GL_CULL_FACE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glEnable(GL_CULL_FACE);
        }
    }

    public void updateVSync() {
        glfwSwapInterval(vSync ? 1 : 0);
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