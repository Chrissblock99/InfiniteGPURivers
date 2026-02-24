package me.chriss99

import glm_.vec2.Vec2d
import me.chriss99.ImageWriter.writeImageHeightMap
import glm_.vec3.Vec3
import me.chriss99.render.ColoredVAOGenerator
import org.lwjgl.glfw.GLFW
import kotlin.math.*

class InputController(private val inputDeviceManager: InputDeviceManager, private val main: Main) {
    private val movementDirection = Vec3()
    private var movementSpeed = 3f

    init {
        setupCallBacks()
    }

    private fun setupCallBacks() {
        inputDeviceManager.addKeyPressCallback(GLFW.GLFW_KEY_W) { movementDirection.z++ }
        inputDeviceManager.addKeyPressCallback(GLFW.GLFW_KEY_S) { movementDirection.z-- }
        inputDeviceManager.addKeyPressCallback(GLFW.GLFW_KEY_D) { movementDirection.x++ }
        inputDeviceManager.addKeyPressCallback(GLFW.GLFW_KEY_A) { movementDirection.x-- }
        inputDeviceManager.addKeyPressCallback(GLFW.GLFW_KEY_SPACE) { movementDirection.y++ }
        inputDeviceManager.addKeyPressCallback(GLFW.GLFW_KEY_LEFT_SHIFT) { movementDirection.y-- }

        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_W) { movementDirection.z-- }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_S) { movementDirection.z++ }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_D) { movementDirection.x-- }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_A) { movementDirection.x++ }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_SPACE) { movementDirection.y-- }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_LEFT_SHIFT) { movementDirection.y++ }

        inputDeviceManager.addMouseScrollConsumer { change: Vec2d ->
            movementSpeed =
                max(0.005, 1.5.pow(log(movementSpeed.toDouble(), 1.5) + change.y / 4))
                    .toFloat()
        }

        inputDeviceManager.addMouseRelativeMovementConsumer { change: Vec2d ->
            main.cameraMatrix.yaw -= (change.x / 300.0).toFloat()
            main.cameraMatrix.yaw %= (Math.PI*2).toFloat()
            main.cameraMatrix.pitch = max(
                -Math.PI / 2,
                min(main.cameraMatrix.pitch - (change.y / 300.0), Math.PI / 2)
            ).toFloat()
        }


        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_LEFT) { main.cameraMatrix.roll = (main.cameraMatrix.roll-0.05f) % (Math.PI*2).toFloat() }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_RIGHT) { main.cameraMatrix.roll = (main.cameraMatrix.roll+0.05f) % (Math.PI*2).toFloat() }

        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_O) { main.cameraMatrix.FOV++ }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_P) { main.cameraMatrix.FOV-- }

        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_U) { main.cameraMatrix.zNear -= 0.05f }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_I) { main.cameraMatrix.zNear += 0.05f }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_J) { main.cameraMatrix.zFar -= 0.05f }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_K) { main.cameraMatrix.zFar += 0.05f }

        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_V) { main.window.vSync = !main.window.vSync }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_E) { main.window.wireFrame = !main.window.wireFrame }

        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_M) { main.playerCenteredRenderer.chunkRenderDistance++ }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_N) { main.playerCenteredRenderer.chunkRenderDistance-- }



        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_X) { println("${main.cameraMatrix.position} (${main.cameraMatrix.pitch}, ${main.cameraMatrix.yaw})") }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_T) { main.simulateErosion = !main.simulateErosion }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_I) { main.renderIterations = !main.renderIterations }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_R) { main.primitiveErosion() }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_G) {
            val chunkSize = main.gpuAlgorithm.iteration.chunkSize
            main.vaoList.add(
                ColoredVAOGenerator.iterabilityInfoToCrossVAO(
                main.erosionManager.usedArea.srcPos / chunkSize, main.erosionManager.iterabilityInfoCopy, chunkSize))
        }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_H) {
            main.vaoList.forEach { it.delete() }
            main.vaoList.clear()
        }
        inputDeviceManager.addKeyReleaseCallback(GLFW.GLFW_KEY_F) {
            main.erosionManager.downloadMap()
            writeImageHeightMap(
                main.gpuAlgorithm.bedrock.world.readArea(main.erosionManager.usedArea) as Float2DBufferWrapper,
                "terrain",
                true
            )
            writeImageHeightMap(
                main.gpuAlgorithm.stream.world.readArea(main.erosionManager.usedArea) as Float2DBufferWrapper,
                "water",
                false
            )
        }
    }

    fun update(deltaTime: Double) {
        main.cameraMatrix.position.plusAssign(main.cameraMatrix.copy(yaw = -main.cameraMatrix.yaw).yawMatrix.toMat3() * (movementDirection * (movementSpeed * deltaTime)))
    }
}