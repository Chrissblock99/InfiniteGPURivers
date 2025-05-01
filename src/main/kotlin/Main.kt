package me.chriss99

import me.chriss99.erosion.ErosionManager
import me.chriss99.erosion.GPUTerrainEroder
import me.chriss99.program.*
import me.chriss99.util.FrameCounter
import me.chriss99.util.Util
import me.chriss99.worldmanagement.ErosionDataStorage
import org.joml.Vector2i
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.*

class Main(
    worldName: String,
    chunkSize: Int, regionSize: Int, iterationChunkSize: Int, iterationRegionSize: Int,
    chunkRenderDistance: Int, iterationRenderDistance: Int,
    initErosionArea: Area
) {
    val window: Window = Window()

    val vaoList: ArrayList<ColoredVAO> = ArrayList<ColoredVAO>(listOf<ColoredVAO>()) //test case for rendering
    val vaoListProgram: ListRenderer<ColoredVAO>
    val playerCenteredRenderer: PositionCenteredRenderer<TerrainVAO>
    var renderIterations: Boolean = false
    val iterationRenderer: PositionCenteredRenderer<IterationVAO>
    val tessProgram: TessProgram

    val worldStorage: ErosionDataStorage
    val erosionManager: ErosionManager

    val gpuTerrainEroder: GPUTerrainEroder
    var simulateErosion: Boolean = false

    val cameraMatrix: CameraMatrix = CameraMatrix()
    val inputController: InputController

    val frameCounter: FrameCounter


    init {
        cameraMatrix.aspectRatio = window.aspectRatio

        worldStorage = ErosionDataStorage(worldName, chunkSize, regionSize, iterationChunkSize, iterationRegionSize)
        gpuTerrainEroder = GPUTerrainEroder(worldStorage, initErosionArea.size, initErosionArea)
        erosionManager = ErosionManager(gpuTerrainEroder, worldStorage.iterationInfo)

        vaoListProgram = ListRenderer(ColoredVAORenderer(cameraMatrix), vaoList)
        playerCenteredRenderer = PositionCenteredRenderer(TerrainVAORenderer(cameraMatrix), { srcPos1, chunkSize1 ->
            var chunkSize1 = chunkSize1
            chunkSize1++
            val area: Area = Area(srcPos1, chunkSize1)
            val terrain: Float2DBufferWrapper = worldStorage.terrain.readArea(area) as Float2DBufferWrapper
            val water: Float2DBufferWrapper = worldStorage.water.readArea(area) as Float2DBufferWrapper
            TerrainVAOGenerator.heightMapToSimpleVAO(terrain, water, srcPos1, 1)
        }, cameraMatrix.position, worldStorage.chunkSize, chunkRenderDistance, initErosionArea)
        iterationRenderer = PositionCenteredRenderer(
            IterationVAORenderer(cameraMatrix),
            { vector2i, chunkSize1 ->
                IterationVAOGenerator.heightMapToIterationVAO(
                    vector2i,
                    Vector2i(chunkSize1),
                    worldStorage.iterationInfo
                )
            },
            cameraMatrix.position, worldStorage.iterationChunkSize, iterationRenderDistance
        )
        tessProgram = TessProgram(cameraMatrix, initErosionArea)

        inputController = InputController(window.inputDeviceManager, this)
        GLUtil.setupDebugMessageCallback()

        frameCounter = FrameCounter(1.0 / 60.0)
    }

    fun primitiveErosion() {
        val pos: Vector2i = Vector2i(
            Util.properIntDivide(
                Vector2i(cameraMatrix.position.x.toInt(), cameraMatrix.position.z.toInt()),
                worldStorage.chunkSize
            )
        )
        if (erosionManager.findIterate(pos, 2000, 100 * 1000 * 1000 / 60)) {
            tessProgram.setArea(gpuTerrainEroder.getUsedArea())

            iterationRenderer.reloadAll()
        } else simulateErosion = false
    }

    private fun loop() {
        while (!window.shouldClose()) {
            inputController.update(frameCounter.deltaTime)

            window.updateWindowSize()
            cameraMatrix.aspectRatio = window.aspectRatio

            playerCenteredRenderer.updateLoadedChunks(cameraMatrix.position, gpuTerrainEroder.getUsedArea())
            if (renderIterations) iterationRenderer.updateLoadedChunks(Vector3f(cameraMatrix.position).div(worldStorage.iterationInfo.chunkSize.toFloat()))

            window.clearBuffers()

            vaoListProgram.render()
            tessProgram.renderTerrain()
            playerCenteredRenderer.render()
            tessProgram.renderWater()
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
        gpuTerrainEroder.downloadMap()
        worldStorage.unloadAll()
        println("Saved world in " + (GLFW.glfwGetTime() - lastTime) + " seconds.")
    }

    private fun cleanGL() {
        vaoListProgram.delete()
        playerCenteredRenderer.delete()
        iterationRenderer.delete()
        tessProgram.delete()

        worldStorage.cleanGL()
        gpuTerrainEroder.delete()
    }

    companion object {
        var main: Main? = null

        @JvmStatic
        fun main(args: Array<String>) {
            GLFW.glfwInit()
            val start = GLFW.glfwGetTime()

            main = Main(
                "test64",
                64, 10, 64, 10,
                7, 2,
                Area(60 * 64)
            )

            println("Started after: " + (GLFW.glfwGetTime() - start))
            main!!.loop()
            main!!.saveWorld()
            println("Window closed")

            main!!.cleanGL()
        }
    }
}