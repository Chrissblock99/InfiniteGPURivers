package me.chriss99;

import me.chriss99.program.*;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;

import java.util.*;


public class Main {
    public final Window window;

    public final  ListRenderer<ColoredVAO> vaoListProgram;
    public final  PositionCenteredRenderer<TerrainVAO> playerCenteredRenderer;
    public boolean renderIterations = false;
    public final  PositionCenteredRenderer<IterationVAO> iterationRenderer;
    public final TessProgram tessProgram;

    public final ErosionDataStorage worldStorage;
    public final ErosionManager erosionManager;

    public final GPUTerrainEroder gpuTerrainEroder;
    public boolean simulateErosion = false;

    public final CameraMatrix cameraMatrix = new CameraMatrix();
    public final InputController inputController;

    private double deltaTime = 1d/60d;


    public static void main(String[] args) {
        glfwInit();
        double start = glfwGetTime();

        Main main = new Main("test64",
                64, 10, 64, 10,
                7, 2,
                new Vector2i(), new Vector2i(8*64));

        System.out.println("Started after: " + (glfwGetTime() - start));
        main.loop();
        System.out.println("Window closed");

        main.cleanGL();
    }

    public Main(String worldName,
                int chunkSize, int regionSize, int iterationChunkSize, int iterationRegionSize,
                int chunkRenderDistance, int iterationRenderDistance,
                Vector2i srcPos, Vector2i size) {
        window = new Window();
        cameraMatrix.aspectRatio = window.getAspectRatio();

        worldStorage = new ErosionDataStorage(worldName, chunkSize, regionSize, iterationChunkSize, iterationRegionSize);
        gpuTerrainEroder = new GPUTerrainEroder(worldStorage, srcPos, new Vector2i(size).add(1, 1), new Vector2i(size).add(1, 1));
        erosionManager = new ErosionManager(gpuTerrainEroder, worldStorage.iterationInfo);

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
        tessProgram = new TessProgram(cameraMatrix, srcPos, size.x, size.y);

        inputController = new InputController(window.inputDeviceManager, this);
        GLUtil.setupDebugMessageCallback();
    }

    public void primitiveErosion() {
        Vector2i pos = new Vector2i(Util.properIntDivide((int) cameraMatrix.position.x, worldStorage.chunkSize), Util.properIntDivide((int) cameraMatrix.position.z, worldStorage.chunkSize));
        if (erosionManager.findIterate(pos.sub(50, 50), new Vector2i(100), 2000)) {
            tessProgram.setSrcPos(gpuTerrainEroder.getSrcPos());

            iterationRenderer.reloadAll();
        } else simulateErosion = false;
    }

    private void loop() {
        double lastTime = glfwGetTime();
        double lastFramePrint = Double.NEGATIVE_INFINITY;
        LinkedList<Double> frames = new LinkedList<>();

        while(!window.shouldClose()) {
            inputController.update(deltaTime);
            playerCenteredRenderer.updateLoadedChunks(cameraMatrix.position, gpuTerrainEroder.getSrcPos(), gpuTerrainEroder.getSize());
            if (renderIterations)
                iterationRenderer.updateLoadedChunks(new Vector3f(cameraMatrix.position).div(64));

            window.clearBuffers();

            vaoListProgram.render();
            tessProgram.renderTerrain();
            playerCenteredRenderer.render();
            tessProgram.renderWater();
            if (renderIterations)
                iterationRenderer.render();

            window.swapBuffers();

            if (simulateErosion)
                primitiveErosion();
                //gpuTerrainEroder.erosionSteps(simulationStepsPerFrame, true, true, true, true);

            window.pollEvents();


            double currentTime = glfwGetTime();
            frames.add(currentTime);
            Iterator<Double> iterator = frames.iterator();
            for (int i = 0; i < frames.size(); i++)
                if (currentTime - iterator.next() >= 1)
                    iterator.remove();
                else break;

            deltaTime = currentTime - lastTime;
            if (!window.getVSync() && currentTime - lastFramePrint > .5) {
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

    private void cleanGL() {
        vaoListProgram.delete();
        playerCenteredRenderer.delete();
        iterationRenderer.delete();
        tessProgram.delete();

        worldStorage.cleanGL();
        gpuTerrainEroder.delete();
    }
}