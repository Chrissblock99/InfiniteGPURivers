package me.chriss99

import me.chriss99.erosion.ErosionManager
import me.chriss99.program.*
import me.chriss99.util.FrameCounter
import me.chriss99.util.Util
import me.chriss99.worldmanagement.ErosionDataStorage
import glm_.vec2.Vec2i
import glm_.vec3.swizzle.xz
import me.chriss99.render.ColoredVAO
import me.chriss99.render.IterationVAOGenerator
import me.chriss99.render.TerrainVAOGenerator
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.*
import kotlin.collections.ArrayList

class Main(
    worldName: String,
    chunkRenderDistance: Int, val chunkLoadBufferDistance: Int, iterationRenderDistance: Int,
    maxErosionSize: Vec2i, targetIteration: Int
) {
    val window = Window("InfiniteGPURivers")
    val cameraMatrix = CameraMatrix(aspectRatio = window.aspectRatio)

    val worldStorage = ErosionDataStorage(worldName, chunkRenderDistance, chunkLoadBufferDistance, Vec2i(cameraMatrix.position.xz))
    var simulateErosion = false
    val erosionManager = ErosionManager(Vec2i(cameraMatrix.position.xz), maxErosionSize, worldStorage, targetIteration)

    val vaoList = ArrayList<ColoredVAO>(listOf<ColoredVAO>()) //test case for rendering
    val vaoListProgram = ListRenderer(ColoredVAORenderer(cameraMatrix), vaoList)
    val playerCenteredRenderer = PositionCenteredRenderer(TerrainVAORenderer(cameraMatrix), { srcPos1, chunkSize1 ->
        val area = Area(srcPos1, chunkSize1 + 1)
        val terrain = worldStorage.height.readArea(area) as Float2DBufferWrapper
        val water = Float2DBufferWrapper(area.size) //worldStorage.drainageArea.readArea(area) as Float2DBufferWrapper
        TerrainVAOGenerator.heightMapToSimpleVAO(terrain, water, srcPos1, 1)
    }, cameraMatrix.position, worldStorage.chunkSize, chunkRenderDistance, erosionManager.usedArea)
    val iterationRenderer = PositionCenteredRenderer(
        IterationVAORenderer(cameraMatrix),
        { vec2i, chunkSize1 ->
            IterationVAOGenerator.heightMapToIterationVAO(
                vec2i,
                Vec2i(chunkSize1),
                worldStorage.iterationInfo
            )
        },
        cameraMatrix.position, worldStorage.iterationChunkSize, iterationRenderDistance
    )
    var renderIterations = false
    val tessProgram = TessProgram(cameraMatrix, erosionManager.usedArea)

    val inputController = InputController(window.inputDeviceManager, this)
    init { GLUtil.setupDebugMessageCallback() }

    val frameCounter = FrameCounter(1.0 / 60.0)


    fun primitiveErosion() {
        val pos = Util.floorDiv(Vec2i(cameraMatrix.position.xz), worldStorage.chunkSize)
        if (erosionManager.findIterate(pos, 100 * 1000 * 1000 / 60))
            iterationRenderer.reloadAll()
        else simulateErosion = false
        tessProgram.area = erosionManager.usedArea
    }

    private fun loop() {
        while (!window.shouldClose()) {
            inputController.update(frameCounter.deltaTime)

            window.updateWindowSize()
            cameraMatrix.aspectRatio = window.aspectRatio

            playerCenteredRenderer.updateLoadedChunks(cameraMatrix.position, erosionManager.usedArea)
            if (renderIterations)
                iterationRenderer.updateLoadedChunks(cameraMatrix.position / worldStorage.iterationInfo.chunkSize)
            worldStorage.manageLoad(playerCenteredRenderer.chunkRenderDistance, chunkLoadBufferDistance, iterationRenderer.chunkRenderDistance, Vec2i(cameraMatrix.position.xz))

            window.clearBuffers()

            vaoListProgram.render()
            tessProgram.renderTerrain()
            playerCenteredRenderer.render()
            //tessProgram.renderWater()
            if (renderIterations) iterationRenderer.render()

            window.swapBuffers()

            if (simulateErosion) primitiveErosion()

            window.pollEvents()

            frameCounter.frameDone()
            if (!window.vSync) frameCounter.reportFPS()
        }
    }

    private fun saveWorld() {
        println("Finishing erosion tasks...")
        var lastTime = GLFW.glfwGetTime()
        erosionManager.finishRunningTasks()
        val currentTime = GLFW.glfwGetTime()
        println("ran in " + (currentTime - lastTime) + " seconds. (Probably inaccurate because this doesn't sync with the GPU)")
        lastTime = currentTime

        println("Saving world...")
        worldStorage.unloadAll()
        println("Saved world in " + (GLFW.glfwGetTime() - lastTime) + " seconds.")
    }

    private fun cleanGL() {
        vaoListProgram.delete()
        playerCenteredRenderer.delete()
        iterationRenderer.delete()
        tessProgram.delete()

        worldStorage.cleanGL()
        erosionManager.delete()
    }

    companion object {
        lateinit var main: Main

        @JvmStatic
        fun main(args: Array<String>) {
            GLFW.glfwInit()
            val start = GLFW.glfwGetTime()

            main = Main(
                "test64",
                7, 4, 2,
                Vec2i(60 * 64), 2000
            )

            println("Started after: " + (GLFW.glfwGetTime() - start))
            main.loop()
            main.saveWorld()
            println("Window closed")

            main.cleanGL()
        }
    }
}