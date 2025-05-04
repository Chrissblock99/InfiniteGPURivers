package me.chriss99

import glm_.vec2.Vec2d
import org.lwjgl.glfw.GLFW

class InputDeviceManager(windowId: Long) {
    private val keyPressCallbacks = HashMap<Int, ArrayList<() -> Unit>>()
    private val keyReleaseCallbacks = HashMap<Int, ArrayList<() -> Unit>>()
    private val mouseAbsoluteMovementConsumers = ArrayList<(pos: Vec2d) -> Unit>()
    private val mouseRelativeMovementConsumers = ArrayList<(change: Vec2d) -> Unit>()
    private val mouseScrollConsumers = ArrayList<(change: Vec2d) -> Unit>()

    private var lastMousePos = Vec2d(Double.NaN)

    init {
        GLFW.glfwSetKeyCallback(windowId) { _, key: Int, _, action: Int, _ ->
            val callbacks: ArrayList<() -> Unit>? = when (action) {
                GLFW.GLFW_PRESS -> keyPressCallbacks[key]
                GLFW.GLFW_RELEASE -> keyReleaseCallbacks[key]
                else -> null
            }

            callbacks?.forEach { it() }
        }

        GLFW.glfwSetCursorPosCallback(windowId) { _, x: Double, y: Double ->
            val pos = Vec2d(x, y)
            mouseAbsoluteMovementConsumers.forEach { it(pos) }

            if (java.lang.Double.isNaN(lastMousePos.x)) {
                lastMousePos = pos
                return@glfwSetCursorPosCallback
            }

            val change = pos - lastMousePos
            mouseRelativeMovementConsumers.forEach { it(change) }

            lastMousePos = pos
        }

        GLFW.glfwSetScrollCallback(windowId) { _, dx: Double, dy: Double ->
            val change = Vec2d(dx, dy)
            mouseScrollConsumers.forEach { it(change) }
        }
    }


    fun addKeyPressCallback(key: Int, runnable: () -> Unit) {
        keyPressCallbacks.computeIfAbsent(key) { _ -> ArrayList() }.add(runnable)
    }

    fun addKeyReleaseCallback(key: Int, runnable: () -> Unit) {
        keyReleaseCallbacks.computeIfAbsent(key) { _ -> ArrayList() }.add(runnable)
    }

    fun addMouseAbsoluteMovementConsumer(consumer: (pos: Vec2d) -> Unit) {
        mouseAbsoluteMovementConsumers.add(consumer)
    }

    fun addMouseRelativeMovementConsumer(consumer: (change: Vec2d) -> Unit) {
        mouseRelativeMovementConsumers.add(consumer)
    }

    fun addMouseScrollConsumer(consumer: (change: Vec2d) -> Unit) {
        mouseScrollConsumers.add(consumer)
    }

    fun removeKeyPressCallback(key: Int, runnable: () -> Unit): Boolean {
        return keyPressCallbacks.computeIfAbsent(key) { _ -> ArrayList() }.remove(runnable)
    }

    fun removeKeyReleaseCallback(key: Int, runnable: () -> Unit): Boolean {
        return keyReleaseCallbacks.computeIfAbsent(key) { _ -> ArrayList() }.remove(runnable)
    }

    fun removeMouseAbsoluteMovementConsumer(consumer: (pos: Vec2d) -> Unit): Boolean {
        return mouseAbsoluteMovementConsumers.remove(consumer)
    }

    fun removeMouseRelativeMovementConsumer(consumer: (change: Vec2d) -> Unit): Boolean {
        return mouseRelativeMovementConsumers.remove(consumer)
    }

    fun removeMouseScrollConsumer(consumer: (change: Vec2d) -> Unit): Boolean {
        return mouseScrollConsumers.remove(consumer)
    }
}