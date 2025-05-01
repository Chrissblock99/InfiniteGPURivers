package me.chriss99

import me.chriss99.ImageWriter.writeImageHeightMap
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW
import kotlin.math.*

class InputController(private val inputDeviceManager: InputDeviceManager, private val main: Main) {
    private val movementDirection = Vec3()
    private var movementSpeed = 3f

    init {
        setupCallBacks()
    }

    private fun setupCallBacks() {
        inputDeviceManager.addKeyPressRunnable(GLFW.GLFW_KEY_W) { movementDirection.z++ }
        inputDeviceManager.addKeyPressRunnable(GLFW.GLFW_KEY_S) { movementDirection.z-- }
        inputDeviceManager.addKeyPressRunnable(GLFW.GLFW_KEY_D) { movementDirection.x++ }
        inputDeviceManager.addKeyPressRunnable(GLFW.GLFW_KEY_A) { movementDirection.x-- }
        inputDeviceManager.addKeyPressRunnable(GLFW.GLFW_KEY_SPACE) { movementDirection.y++ }
        inputDeviceManager.addKeyPressRunnable(GLFW.GLFW_KEY_LEFT_SHIFT) { movementDirection.y-- }

        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_W) { movementDirection.z-- }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_S) { movementDirection.z++ }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_D) { movementDirection.x-- }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_A) { movementDirection.x++ }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_SPACE) { movementDirection.y-- }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_LEFT_SHIFT) { movementDirection.y++ }

        inputDeviceManager.addMouseScrollConsumer { dx: Double?, dy: Double ->
            movementSpeed =
                max(0.005, 1.5.pow(log(movementSpeed.toDouble(), 1.5) + dy / 4))
                    .toFloat()
        }

        inputDeviceManager.addMouseRelativeMovementConsumer { dx: Double, dy: Double ->
            main.cameraMatrix.yaw -= (dx / 300.0).toFloat()
            main.cameraMatrix.pitch = max(
                -Math.PI / 2,
                min(main.cameraMatrix.pitch - (dy / 300.0), Math.PI / 2)
            ).toFloat()
        }


        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_LEFT) { main.cameraMatrix.roll -= 0.05f }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_RIGHT) { main.cameraMatrix.roll += 0.05f }

        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_O) { main.cameraMatrix.FOV++ }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_P) { main.cameraMatrix.FOV-- }

        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_U) { main.cameraMatrix.zNear -= 0.05f }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_I) { main.cameraMatrix.zNear += 0.05f }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_J) { main.cameraMatrix.zFar -= 0.05f }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_K) { main.cameraMatrix.zFar += 0.05f }

        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_V) { main.window.vSync = !main.window.vSync }
        inputDeviceManager.addKeyReleaseRunnable(
            GLFW.GLFW_KEY_E
        ) { main.window.wireFrame = !main.window.wireFrame }

        inputDeviceManager.addKeyReleaseRunnable(
            GLFW.GLFW_KEY_M
        ) { main.playerCenteredRenderer.chunkRenderDistance++ }
        inputDeviceManager.addKeyReleaseRunnable(
            GLFW.GLFW_KEY_N
        ) { main.playerCenteredRenderer.chunkRenderDistance-- }



        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_T) { main.simulateErosion = !main.simulateErosion }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_I) { main.renderIterations = !main.renderIterations }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_R) { main.primitiveErosion() }
        inputDeviceManager.addKeyReleaseRunnable(GLFW.GLFW_KEY_F) {
            main.gpuTerrainEroder.downloadMap()
            writeImageHeightMap(
                main.worldStorage.terrain.readArea(main.gpuTerrainEroder.getUsedArea()) as Float2DBufferWrapper,
                "terrain",
                true
            )
            writeImageHeightMap(
                main.worldStorage.water.readArea(main.gpuTerrainEroder.getUsedArea()) as Float2DBufferWrapper,
                "water",
                false
            )
        }
    }

    fun update(deltaTime: Double) {
        main.cameraMatrix.position.plusAssign(main.cameraMatrix.copy(yaw = -main.cameraMatrix.yaw).yawMatrix.toMat3() * (movementDirection * (movementSpeed * deltaTime)))
    }
}