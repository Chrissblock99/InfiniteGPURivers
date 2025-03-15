package me.chriss99.util;

import java.util.Iterator;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class FrameCounter {
    private double deltaTime;

    private double lastTime;
    private double lastFramePrint = Double.NEGATIVE_INFINITY;
    private final LinkedList<Double> frames = new LinkedList<>();

    public FrameCounter(double initDeltaTime) {
        deltaTime = initDeltaTime;

        lastTime = glfwGetTime();
    }

    public void frameDone() {
        double currentTime = glfwGetTime();

        frames.add(currentTime);
        Iterator<Double> iterator = frames.iterator();
        for (int i = 0; i < frames.size(); i++)
            if (currentTime - iterator.next() >= 1)
                iterator.remove();
            else break;

        deltaTime = currentTime - lastTime;
        lastTime = currentTime;
    }

    public void reportFPS() {
        double currentTime = glfwGetTime();

        if (currentTime - lastFramePrint > .5) {
            System.out.println(frames.size() + "   " + Math.round(1/deltaTime) + "   " + deltaTime*1000);
            lastFramePrint = currentTime;
        }
    }

    public double getDeltaTime() {
        return deltaTime;
    }
}
