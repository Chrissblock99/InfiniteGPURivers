package me.chriss99.util

import org.lwjgl.glfw.GLFW
import java.util.*

class FrameCounter(var deltaTime: Double) {
    private var lastTime: Double
    private var lastFramePrint = Double.NEGATIVE_INFINITY
    private val frames = LinkedList<Double>()

    init {
        lastTime = GLFW.glfwGetTime()
    }

    fun frameDone() {
        val currentTime = GLFW.glfwGetTime()

        frames.add(currentTime)
        frames.removeIf { currentTime - it >= 1 }

        deltaTime = currentTime - lastTime
        lastTime = currentTime
    }

    fun reportFPS() {
        val currentTime = GLFW.glfwGetTime()

        if (currentTime - lastFramePrint > .5) {
            println(frames.size.toString() + "   " + Math.round(1 / deltaTime) + "   " + deltaTime * 1000)
            lastFramePrint = currentTime
        }
    }
}