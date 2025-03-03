package me.chriss99;

import org.joml.Vector2i;

public record Area(Vector2i srcPos, Vector2i endPos) {
    public Area() {
        this(0);
    }

    public Area(int size) {
        this(new Vector2i(size));
    }

    public Area(Vector2i size) {
        this(new Vector2i(), size);
    }

    public Area(Vector2i srcPos, int size) {
        this(srcPos, new Vector2i(size).add(srcPos));
    }

    public Area(Vector2i srcPos, Vector2i endPos) {
        if (srcPos.x > endPos.x || srcPos.y > endPos.y)
            throw new IllegalArgumentException("Src and endPos do not form a valid area! srcPos: " + srcPos + " endPos: " + endPos);

        this.srcPos = new Vector2i(srcPos);
        this.endPos = new Vector2i(endPos);
    }

    public boolean contains(Area area) {
        return area.srcPos.x >= srcPos.x && area.srcPos.y >= srcPos.y && area.endPos.x <= endPos.x && area.endPos.y <= endPos.y;
    }

    public boolean contains(Vector2i pos) {
        return pos.x >= srcPos.x && pos.y >= srcPos.y && pos.x < endPos.x && pos.y < endPos.y;
    }

    public Area outset(int distance) {
        return increase(distance, distance, distance, distance);
    }

    public Area increase(int right, int up, int left, int down) {
        return new Area(new Vector2i(srcPos).sub(left, down), new Vector2i(endPos).add(right, up));
    }

    public Area add(Vector2i shift) {
        return new Area(new Vector2i(srcPos).add(shift), new Vector2i(endPos).add(shift));
    }

    public Area sub(Vector2i shift) {
        return new Area(new Vector2i(srcPos).sub(shift), new Vector2i(endPos).sub(shift));
    }

    public Area mul(int scalar) {
        return new Area(new Vector2i(srcPos).mul(scalar), new Vector2i(endPos).mul(scalar));
    }

    public Area div(int scalar) {
        return new Area(new Vector2i(srcPos).div(scalar), new Vector2i(endPos).div(scalar));
    }


    public Vector2i getSize() {
        return new Vector2i(endPos).sub(srcPos);
    }

    public int getWidth() {
        return endPos.x - srcPos.x;
    }

    public int getHeight() {
        return endPos.y - srcPos.y;
    }

    public int getArea() {
        return getWidth()*getHeight();
    }

    @Override
    public Vector2i srcPos() {
        return new Vector2i(srcPos);
    }

    @Override
    public Vector2i endPos() {
        return new Vector2i(endPos);
    }

    public Area copy() {
        return new Area(srcPos, endPos);
    }
}
