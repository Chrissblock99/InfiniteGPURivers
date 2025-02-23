package me.chriss99;

import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;

public class Window {
    private final long windowId;

    private int width;
    private int height;

    public final InputDeviceManager inputDeviceManager;

    private boolean vSync;
    private boolean wireFrame;

    public Window() {
        width = glfwGetVideoMode(glfwGetPrimaryMonitor()).width();
        height = glfwGetVideoMode(glfwGetPrimaryMonitor()).height();

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        windowId = glfwCreateWindow(width, height, "GLFW OpenGL Window", 0, 0);

        glfwMakeContextCurrent(windowId);
        System.out.println("OpenGL 4.5 Supported: " + GL.createCapabilities().OpenGL45);
        glViewport(0, 0, width, height);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        inputDeviceManager = new InputDeviceManager(windowId);
        glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        setVSync(true);
        setWireFrame(false);

        glfwShowWindow(windowId);
    }

    public float getAspectRatio() {
        return (float) height / (float) width;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowId);
    }

    public void swapBuffers() {
        glfwSwapBuffers(windowId);
    }

    public void clearBuffers() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void setWireFrame(boolean wireFrame) {
        this.wireFrame = wireFrame;
        if (wireFrame) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glDisable(GL_CULL_FACE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glEnable(GL_CULL_FACE);
        }
    }

    public void setVSync(boolean vSync) {
        this.vSync = vSync;
        glfwSwapInterval(vSync ? 1 : 0);
    }

    public boolean getVSync() {
        return vSync;
    }

    public boolean getWireFrame() {
        return wireFrame;
    }
}
