package me.chriss99;

import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class MovementController {
    private final CameraMatrix cameraMatrix;
    private final InputDeviceManager inputDeviceManager;
    private final Vector3f movementDirection = new Vector3f();
    private float movementSpeed = 3f;

    public MovementController(InputDeviceManager inputDeviceManager, CameraMatrix cameraMatrix) {
        this.cameraMatrix = cameraMatrix;
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
            cameraMatrix.yaw -= (float) (dx/300d);
            cameraMatrix.pitch = (float) Math.max(-Math.PI/2, Math.min(cameraMatrix.pitch - (dy/300d), Math.PI/2));
        });


        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_LEFT, () -> cameraMatrix.roll -= 0.05f);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_RIGHT, () -> cameraMatrix.roll += 0.05f);

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_O, () -> cameraMatrix.FOV++);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_P, () -> cameraMatrix.FOV--);

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_U, () -> cameraMatrix.zNear -= 0.05f);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_I, () -> cameraMatrix.zNear += 0.05f);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_J, () -> cameraMatrix.zFar -= 0.05f);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_K, () -> cameraMatrix.zFar += 0.05f);

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_V, () -> {
            Main.vSync = !Main.vSync;
            Main.updateVSync();
        });



        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_T, () -> Main.simulateThermal = !Main.simulateThermal);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_H, () -> Main.simulateHydraulic = !Main.simulateHydraulic);
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_G, () -> Main.heightMapTransformer.rain = !Main.heightMapTransformer.rain);

        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_R, () -> {
            Main.terrainData = new TerrainData(HeightMapGenerator.pillar(100, 100));
            Main.updateTerrainVAOs();
        });
        inputDeviceManager.addKeyReleaseRunnable(GLFW_KEY_F, () -> {
            Main.terrainData = new TerrainData(HeightMapGenerator.pillars(100, 100));
            Main.updateTerrainVAOs();
        });
    }

    public void update() {
        cameraMatrix.position.add(new Vector3f(movementDirection).mul((float) (movementSpeed * Main.deltaTime)).rotateY(-cameraMatrix.yaw));
    }

    private static double log(double b, double x) {
        return Math.log(x) / Math.log(b);
    }
}
