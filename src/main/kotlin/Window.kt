package me.chriss99

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

class Window(title: String) {
    private val windowId: Long

    private var width: Int
    private var height: Int

    val inputDeviceManager: InputDeviceManager

    var vSync: Boolean = false
        set(vSync) {
            field = vSync
            glfwSwapInterval(if (vSync) 1 else 0)
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
        width = glfwGetVideoMode(glfwGetPrimaryMonitor())!!.width()
        height = glfwGetVideoMode(glfwGetPrimaryMonitor())!!.height()

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        windowId = glfwCreateWindow(width, height, title, 0, 0)

        glfwMakeContextCurrent(windowId)
        if (!GL.createCapabilities().OpenGL45)
            System.err.println(
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
        glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)

        vSync = true
        wireFrame = false

        glfwShowWindow(windowId)
    }

    fun updateWindowSize() {
        val width = IntArray(1)
        val height = IntArray(1)

        glfwGetWindowSize(windowId, width, height)

        this.width = width[0]
        this.height = height[0]

        glViewport(0, 0, this.width, this.height)
    }

    val aspectRatio: Float
        get() = height.toFloat() / width.toFloat()

    fun shouldClose() = glfwWindowShouldClose(windowId)
    fun swapBuffers() = glfwSwapBuffers(windowId)
    fun clearBuffers() = glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    fun pollEvents() = glfwPollEvents()
}