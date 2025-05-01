package me.chriss99;

import me.chriss99.glabstractions.VAOImpl;

public class ColoredVAO extends VAOImpl {
    public ColoredVAO(double[] triangle, double[] color, int[] index) {
        super(index, 3, triangle, color);
    }

    public void updatePositions(double[] positions) {
        updateVertices(0, positions);
    }

    public void updateColors(double[] colors) {
        updateVertices(1, colors);
    }

    public void updateIndex(int[] index) {
        updateIndices(index);
    }
}
