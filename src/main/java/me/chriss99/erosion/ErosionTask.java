package me.chriss99.erosion;

import me.chriss99.Area;

public class ErosionTask {
    private final GPUTerrainEroder eroder;

    private final Area area;
    private final int steps;
    private final boolean lFlat;
    private final boolean rFlat;
    private final boolean fFlat;
    private final boolean bFlat;

    private Area currentArea;
    private int currentStep;

    public ErosionTask(GPUTerrainEroder eroder, Area area, int steps, boolean lFlat, boolean rFlat, boolean fFlat, boolean bFlat) {
        this.eroder = eroder;
        this.area = area.copy();
        this.steps = steps;
        this.lFlat = lFlat;
        this.rFlat = rFlat;
        this.fFlat = fFlat;
        this.bFlat = bFlat;

        currentArea = area.increase(rFlat ? 0 : -steps, fFlat ? 0 : -steps, lFlat ? 0 : -steps, bFlat ? 0 : -steps);
        currentStep = 0;
    }

    public boolean erosionStep() {
        if (isDone())
            return true;

        eroder.erode(currentArea);
        currentArea = currentArea.increase(rFlat ? -1 : 1, fFlat ? -1 : 1, lFlat ? -1 : 1, bFlat ? -1 : 1);
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
}
