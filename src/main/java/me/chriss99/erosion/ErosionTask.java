package me.chriss99.erosion;

import me.chriss99.Area;

public class ErosionTask {
    private final GPUTerrainEroder eroder;

    private final Area area;
    private final int steps;
    private final int l;
    private final int r;
    private final int f;
    private final int b;

    private Area currentArea;
    private int currentStep;

    public ErosionTask(GPUTerrainEroder eroder, Area area, int steps, int l, int r, int f, int b) {
        this.eroder = eroder;
        this.area = area.copy();
        this.steps = steps;
        this.l = l;
        this.r = r;
        this.f = f;
        this.b = b;

        currentArea = area.increase(r == 0 ? 0 : -steps, f == 0 ? 0 : -steps, l == 0 ? 0 : -steps, b == 0 ? 0 : -steps);
        currentStep = 0;
    }

    public boolean erosionStep() {
        if (isDone())
            return true;

        eroder.erode(currentArea);
        currentArea = currentArea.increase(r == 0 ? -1 : 1, f == 0 ? -1 : 1, l == 0 ? -1 : 1, b == 0 ? -1 : 1);
        currentStep++;
        return false;
    }

    public boolean hasStarted() {
        return currentStep != 0;
    }

    public boolean isDone() {
        return currentStep >= steps;
    }

    public boolean isRunning() {
        return hasStarted() && !isDone();
    }

    public Area getArea() {
        return area.copy();
    }

    public int getB() {
        return b;
    }

    public int getF() {
        return f;
    }

    public int getR() {
        return r;
    }

    public int getL() {
        return l;
    }
}
