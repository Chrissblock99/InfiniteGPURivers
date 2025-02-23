package me.chriss99;

import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class InputController {
    private final Main main;
    private final InputDeviceManager inputDeviceManager;
    private final Vector3f movementDirection = new Vector3f();
    private float movementSpeed = 3f;

    public InputController(InputDeviceManager inputDeviceManager, Main main) {
        this.main = main;
        this.inputDeviceManager = inputDeviceManager;
        setupCallBacks();
    }

    private void setupCallBacks() {
        inputDeviceManager.addKeyPressRunnable(GLFW_KEY_W, () -> movementDirection.z++);
        inputDeviceManager.addKeyPressRunnable(GLFW_KEY_S, () -> movementDirection.z--);
        inputDeviceManager.addKeyPressRunnable(GLFW_KEY_D, () -> movementDirection.x++);
        inputDeviceManager.addKeyPressRunnable(GLFW_KEY_A, () -> movementDirection.x--);
        inputDeviceManager.addKeyPressRunnable(GLFW_KEY_SPACE, () -> movementDirection.y++);
        inputDeviceManager.addKeyPressRunnable(GLFW_KEY_LEFT_SHIFT, () -> movementDirection.y--);

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_W, () -> movementDirection.z--);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_S, () -> movementDirection.z++);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_D, () -> movementDirection.x--);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_A, () -> movementDirection.x++);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_SPACE, () -> movementDirection.y--);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_LEFT_SHIFT, () -> movementDirection.y++);

        inputDeviceManager.addMouseScrollConsumer((dx, dy) -> movementSpeed = (float) Math.max(0.005, Math.pow(1.5, log(1.5, movementSpeed) + dy/4)));

        inputDeviceManager.addMouseRelativeMovementConsumer((dx, dy) -> {
            main.cameraMatrix.yaw -= (float) (dx/300d);
            main.cameraMatrix.pitch = (float) Math.max(-Math.PI/2, Math.min(main.cameraMatrix.pitch - (dy/300d), Math.PI/2));
        });


        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_LEFT, () -> main.cameraMatrix.roll -= 0.05f);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_RIGHT, () -> main.cameraMatrix.roll += 0.05f);

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_O, () -> main.cameraMatrix.FOV++);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_P, () -> main.cameraMatrix.FOV--);

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_U, () -> main.cameraMatrix.zNear -= 0.05f);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_I, () -> main.cameraMatrix.zNear += 0.05f);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_J, () -> main.cameraMatrix.zFar -= 0.05f);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_K, () -> main.cameraMatrix.zFar += 0.05f);

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_V, () -> main.window.setVSync(!main.window.getVSync()));
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_E, () -> main.window.setWireFrame(!main.window.getWireFrame()));

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_M, () -> {
            main.playerCenteredRenderer.setChunkRenderDistance(main.playerCenteredRenderer.getChunkRenderDistance()+1);
        });
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_N, () -> {
            main.playerCenteredRenderer.setChunkRenderDistance(main.playerCenteredRenderer.getChunkRenderDistance()-1);
        });



        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_T, () -> main.simulateErosion = !main.simulateErosion);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_I, () -> main.renderIterations = !main.renderIterations);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_R, main::primitiveErosion);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_F, () -> {
            main.gpuTerrainEroder.downloadMap();
            ImageWriter.writeImageHeightMap((Float2DBufferWrapper) main.worldStorage.terrain.readArea(main.gpuTerrainEroder.getSrcPos().x, main.gpuTerrainEroder.getSrcPos().y, main.gpuTerrainEroder.getSize().x, main.gpuTerrainEroder.getSize().y), "terrain", true);
            ImageWriter.writeImageHeightMap((Float2DBufferWrapper) main.worldStorage.water.readArea(main.gpuTerrainEroder.getSrcPos().x, main.gpuTerrainEroder.getSrcPos().y, main.gpuTerrainEroder.getSize().x, main.gpuTerrainEroder.getSize().y), "water", false);
        });
    }

    public void update(double deltaTime) {
        main.cameraMatrix.position.add(new Vector3f(movementDirection).mul((float) (movementSpeed * deltaTime)).rotateY(-main.cameraMatrix.yaw));
    }

    private static double log(double b, double x) {
        return Math.log(x) / Math.log(b);
    }
}
