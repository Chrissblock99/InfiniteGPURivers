package me.chriss99.erosion;

import org.joml.Vector2i;

public class ErosionTask {
    private final GPUTerrainEroder eroder;

    private final Vector2i srcPos;
    private final Vector2i endPos;
    private final int steps;
    private final boolean lFlat;
    private final boolean rFlat;
    private final boolean fFlat;
    private final boolean bFlat;

    private int currentStep;

    public ErosionTask(GPUTerrainEroder eroder, Vector2i pos, Vector2i size, int steps, boolean lFlat, boolean rFlat, boolean fFlat, boolean bFlat) {
        this.eroder = eroder;
        srcPos = new Vector2i(pos);
        endPos = new Vector2i(srcPos).add(size);
        this.steps = steps;
        this.lFlat = lFlat;
        this.rFlat = rFlat;
        this.fFlat = fFlat;
        this.bFlat = bFlat;
    }

    public void erode() {
        srcPos.add(lFlat ? 0 : steps, bFlat ? 0 : steps);
        endPos.sub(rFlat ? 0 : steps, fFlat ? 0 : steps);

        for (int i = 0; i < steps; i++) {
            eroder.erode(srcPos, endPos);

            srcPos.add(lFlat ? 1 : -1, bFlat ? 1 : -1);
            endPos.sub(rFlat ? 1 : -1, fFlat ? 1 : -1);
        }
    }
}
