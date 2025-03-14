package me.chriss99;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class InputDeviceManager {
    private final HashMap<Integer, ArrayList<Runnable>> keyPressRunnables = new HashMap<>();
    private final HashMap<Integer, ArrayList<Runnable>> keyReleaseRunnables = new HashMap<>();
    private final ArrayList<BiConsumer<Double, Double>> mouseAbsoluteMovementConsumers = new ArrayList<>();
    private final ArrayList<BiConsumer<Double, Double>> mouseRelativeMovementConsumers = new ArrayList<>();
    private final ArrayList<BiConsumer<Double, Double>> mouseScrollConsumers = new ArrayList<>();

    private double lastMouseX = Double.NaN;
    private double lastMouseY = Double.NaN;

    public InputDeviceManager(long windowId) {
        glfwSetKeyCallback(windowId, (window, key, scancode, action, mods) -> {
            ArrayList<Runnable> runnables = new ArrayList<>();
            switch (action) {
                case GLFW_PRESS -> runnables = keyPressRunnables.get(key);
                case GLFW_RELEASE -> runnables = keyReleaseRunnables.get(key);
            }

            if (runnables == null)
                return;

            for (Runnable runnable : runnables)
                runnable.run();
        });

        glfwSetCursorPosCallback(windowId, (win, x, y) -> {
            for (BiConsumer<Double, Double> consumer : mouseAbsoluteMovementConsumers)
                consumer.accept(x, y);

            if (Double.isNaN(lastMouseX)) {
                lastMouseX = x;
                lastMouseY = y;
                return;
            }

            double dx = x - lastMouseX;
            double dy = y - lastMouseY;
            for (BiConsumer<Double, Double> consumer : mouseRelativeMovementConsumers)
                consumer.accept(dx, dy);

            lastMouseX = x;
            lastMouseY = y;
        });

        glfwSetScrollCallback(windowId, (win, dx, dy) -> {
            for (BiConsumer<Double, Double> consumer : mouseScrollConsumers)
                consumer.accept(dx, dy);
        });
    }


    public void addKeyPressRunnable(int key, Runnable runnable) {
        keyPressRunnables.computeIfAbsent(key, k -> new ArrayList<>()).add(runnable);
    }

    public void addKeyReleaseRunnable(int key, Runnable runnable) {
        keyReleaseRunnables.computeIfAbsent(key, k -> new ArrayList<>()).add(runnable);
    }

    public void addMouseAbsoluteMovementConsumer(BiConsumer<Double, Double> consumer) {
        mouseAbsoluteMovementConsumers.add(consumer);
    }

    public void addMouseRelativeMovementConsumer(BiConsumer<Double, Double> consumer) {
        mouseRelativeMovementConsumers.add(consumer);
    }

    public void addMouseScrollConsumer(BiConsumer<Double, Double> consumer) {
        mouseScrollConsumers.add(consumer);
    }

    public boolean removeKeyPressRunnable(int key, Runnable runnable) {
        return keyPressRunnables.computeIfAbsent(key, k -> new ArrayList<>()).add(runnable);
    }

    public boolean removeKeyReleaseRunnable(int key, Runnable runnable) {
        return keyReleaseRunnables.computeIfAbsent(key, k -> new ArrayList<>()).remove(runnable);
    }

    public boolean removeMouseAbsoluteMovementConsumer(BiConsumer<Double, Double> consumer) {
        return mouseAbsoluteMovementConsumers.add(consumer);
    }

    public boolean removeMouseRelativeMovementConsumer(BiConsumer<Double, Double> consumer) {
        return mouseRelativeMovementConsumers.add(consumer);
    }

    public boolean removeMouseScrollConsumer(BiConsumer<Double, Double> consumer) {
        return mouseScrollConsumers.add(consumer);
    }
}
