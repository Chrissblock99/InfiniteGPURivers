package me.chriss99

import org.lwjgl.glfw.GLFW
import java.util.function.BiConsumer

class InputDeviceManager(windowId: Long) {
    private val keyPressRunnables = HashMap<Int, ArrayList<Runnable>>()
    private val keyReleaseRunnables = HashMap<Int, ArrayList<Runnable>>()
    private val mouseAbsoluteMovementConsumers = ArrayList<BiConsumer<Double, Double>>()
    private val mouseRelativeMovementConsumers = ArrayList<BiConsumer<Double, Double>>()
    private val mouseScrollConsumers = ArrayList<BiConsumer<Double, Double>>()

    private var lastMouseX = Double.NaN
    private var lastMouseY = Double.NaN

    init {
        GLFW.glfwSetKeyCallback(windowId) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            var runnables: ArrayList<Runnable>? = ArrayList()
            when (action) {
                GLFW.GLFW_PRESS -> runnables = keyPressRunnables[key]
                GLFW.GLFW_RELEASE -> runnables = keyReleaseRunnables[key]
            }

            if (runnables == null) return@glfwSetKeyCallback
            for (runnable in runnables) runnable.run()
        }

        GLFW.glfwSetCursorPosCallback(windowId) { win: Long, x: Double, y: Double ->
            for (consumer in mouseAbsoluteMovementConsumers) consumer.accept(x, y)
            if (java.lang.Double.isNaN(lastMouseX)) {
                lastMouseX = x
                lastMouseY = y
                return@glfwSetCursorPosCallback
            }

            val dx = x - lastMouseX
            val dy = y - lastMouseY
            for (consumer in mouseRelativeMovementConsumers) consumer.accept(dx, dy)

            lastMouseX = x
            lastMouseY = y
        }

        GLFW.glfwSetScrollCallback(
            windowId
        ) { win: Long, dx: Double, dy: Double ->
            for (consumer in mouseScrollConsumers) consumer.accept(dx, dy)
        }
    }


    fun addKeyPressRunnable(key: Int, runnable: Runnable) {
        keyPressRunnables.computeIfAbsent(key) { k: Int? -> ArrayList() }.add(runnable)
    }

    fun addKeyReleaseRunnable(key: Int, runnable: Runnable) {
        keyReleaseRunnables.computeIfAbsent(key) { k: Int? -> ArrayList() }.add(runnable)
    }

    fun addMouseAbsoluteMovementConsumer(consumer: BiConsumer<Double, Double>) {
        mouseAbsoluteMovementConsumers.add(consumer)
    }

    fun addMouseRelativeMovementConsumer(consumer: BiConsumer<Double, Double>) {
        mouseRelativeMovementConsumers.add(consumer)
    }

    fun addMouseScrollConsumer(consumer: BiConsumer<Double, Double>) {
        mouseScrollConsumers.add(consumer)
    }

    fun removeKeyPressRunnable(key: Int, runnable: Runnable): Boolean {
        return keyPressRunnables.computeIfAbsent(key) { k: Int? -> ArrayList() }.add(runnable)
    }

    fun removeKeyReleaseRunnable(key: Int, runnable: Runnable): Boolean {
        return keyReleaseRunnables.computeIfAbsent(key) { k: Int? -> ArrayList() }.remove(runnable)
    }

    fun removeMouseAbsoluteMovementConsumer(consumer: BiConsumer<Double, Double>): Boolean {
        return mouseAbsoluteMovementConsumers.add(consumer)
    }

    fun removeMouseRelativeMovementConsumer(consumer: BiConsumer<Double, Double>): Boolean {
        return mouseRelativeMovementConsumers.add(consumer)
    }

    fun removeMouseScrollConsumer(consumer: BiConsumer<Double, Double>): Boolean {
        return mouseScrollConsumers.add(consumer)
    }
}