package me.chriss99

import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

class Window {
    private val windowId: Long

    private var width: Int
    private var height: Int

    val inputDeviceManager: InputDeviceManager

    var vSync: Boolean = false
        set(vSync) {
            field = vSync
            GLFW.glfwSwapInterval(if (vSync) 1 else 0)
        }
    var wireFrame: Boolean = false
        set(wireFrame) {
            field = wireFrame
            if (wireFrame) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                glDisable(GL_CULL_FACE)
            } else {
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
                glEnable(GL_CULL_FACE)
            }
        }

    init {
        width = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!.width()
        height = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!.height()

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        windowId = GLFW.glfwCreateWindow(width, height, "GLFW OpenGL Window", 0, 0)

        GLFW.glfwMakeContextCurrent(windowId)
        if (!GL.createCapabilities().OpenGL45) System.err.println(
            """
                    -----------------------------------------
                    
                    This device does not support OpenGL 4.5!
                    Parts of the program may still work,
                    but expect issues like things not
                    rendering or just crashing.
                    
                    -----------------------------------------
                    
                    """.trimIndent()
        )
        glViewport(0, 0, width, height)
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glEnable(GL_DEPTH_TEST)

        inputDeviceManager = InputDeviceManager(windowId)
        GLFW.glfwSetInputMode(windowId, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED)

        vSync = true
        wireFrame = false

        GLFW.glfwShowWindow(windowId)
    }

    fun updateWindowSize() {
        val width = IntArray(1)
        val height = IntArray(1)

        GLFW.glfwGetWindowSize(windowId, width, height)

        this.width = width[0]
        this.height = height[0]

        glViewport(0, 0, this.width, this.height)
    }

    val aspectRatio: Float
        get() = height.toFloat() / width.toFloat()

    fun shouldClose(): Boolean {
        return GLFW.glfwWindowShouldClose(windowId)
    }

    fun swapBuffers() {
        GLFW.glfwSwapBuffers(windowId)
    }

    fun clearBuffers() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    fun pollEvents() {
        GLFW.glfwPollEvents()
    }
}