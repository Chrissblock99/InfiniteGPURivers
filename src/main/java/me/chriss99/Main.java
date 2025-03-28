package me.chriss99;

import me.chriss99.erosion.ErosionManager;
import me.chriss99.erosion.GPUTerrainEroder;
import me.chriss99.program.*;
import me.chriss99.util.FrameCounter;
import me.chriss99.util.Util;
import me.chriss99.worldmanagement.ErosionDataStorage;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;

import java.util.*;


public class Main {
    public static Main main;

    public final Window window;

    public final ArrayList<ColoredVAO> vaoList = new ArrayList<>(List.of(/*ColoredVAOGenerator.heightMapToSimpleVAO(new double[][]{{0d, 0d, 0d}, {0d, 1d, 0d}, {0d, 0d, 0d}}, -1, 2, true)*/)); //test case for rendering
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

    public final FrameCounter frameCounter;


    public static void main(String[] args) {
        glfwInit();
        double start = glfwGetTime();

        main = new Main("test64TEST",
                64, 10, 64, 10,
                7, 2,
                new Area(40*64));

        System.out.println("Started after: " + (glfwGetTime() - start));
        main.loop();
        main.saveWorld();
        System.out.println("Window closed");

        main.cleanGL();
    }

    public Main(String worldName,
                int chunkSize, int regionSize, int iterationChunkSize, int iterationRegionSize,
                int chunkRenderDistance, int iterationRenderDistance,
                Area initErosionArea) {
        window = new Window();
        cameraMatrix.aspectRatio = window.getAspectRatio();

        worldStorage = new ErosionDataStorage(worldName, chunkSize, regionSize, iterationChunkSize, iterationRegionSize);
        gpuTerrainEroder = new GPUTerrainEroder(worldStorage, initErosionArea.getSize(), initErosionArea);
        erosionManager = new ErosionManager(gpuTerrainEroder, worldStorage.iterationInfo);

        vaoListProgram = new ListRenderer<>(new ColoredVAORenderer(cameraMatrix), vaoList);
        playerCenteredRenderer = new PositionCenteredRenderer<>(new TerrainVAORenderer(cameraMatrix), (srcPos1, chunkSize1) -> {
            chunkSize1++;
            Area area = new Area(srcPos1, chunkSize1);
            Float2DBufferWrapper terrain = (Float2DBufferWrapper) worldStorage.terrain.readArea(area);
            Float2DBufferWrapper water = (Float2DBufferWrapper) worldStorage.water.readArea(area);

            return TerrainVAOGenerator.heightMapToSimpleVAO(terrain, water, srcPos1, 1);
        }, cameraMatrix.position, worldStorage.chunkSize, chunkRenderDistance, initErosionArea);
        iterationRenderer = new PositionCenteredRenderer<>(new IterationVAORenderer(cameraMatrix),
                (vector2i, chunkSize1) -> IterationVAOGenerator.heightMapToIterationVAO(vector2i, new Vector2i(chunkSize1), worldStorage.iterationInfo),
                cameraMatrix.position, worldStorage.iterationChunkSize, iterationRenderDistance);
        tessProgram = new TessProgram(cameraMatrix, initErosionArea);

        inputController = new InputController(window.inputDeviceManager, this);
        GLUtil.setupDebugMessageCallback();

        frameCounter = new FrameCounter(1d/60d);
    }

    public void primitiveErosion() {
        Vector2i pos = new Vector2i(Util.properIntDivide(new Vector2i((int) cameraMatrix.position.x, (int) cameraMatrix.position.z), worldStorage.chunkSize));
        if (erosionManager.findIterate(pos, 2000, 100*1000*1000/60)) {
            tessProgram.setArea(gpuTerrainEroder.getUsedArea());

            iterationRenderer.reloadAll();
        } else simulateErosion = false;
    }

    private void loop() {
        while (!window.shouldClose()) {
            inputController.update(frameCounter.getDeltaTime());

            window.updateWindowSize();
            cameraMatrix.aspectRatio = window.getAspectRatio();

            playerCenteredRenderer.updateLoadedChunks(cameraMatrix.position, gpuTerrainEroder.getUsedArea());
            if (renderIterations)
                iterationRenderer.updateLoadedChunks(new Vector3f(cameraMatrix.position).div(worldStorage.iterationInfo.chunkSize));

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

            window.pollEvents();

            frameCounter.frameDone();
            if (!window.getVSync())
                frameCounter.reportFPS();
        }
    }

    private void saveWorld() {
        System.out.println("Finishing erosion tasks...");
        double lastTime = glfwGetTime();
        erosionManager.finishRunningTasks();
        double currentTime = glfwGetTime();
        System.out.println("ran in " + (currentTime - lastTime) + " seconds. (Probably inaccurate because this doesn't sync with the GPU)");
        lastTime = currentTime;

        System.out.println("Saving world...");
        gpuTerrainEroder.downloadMap();
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