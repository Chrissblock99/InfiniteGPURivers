package me.chriss99.worldmanagement.quadtree;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class Quad<T> {
    private final Quad<T> parent;
    private int greatestNodeDepth;

    private final Vector2i pos;
    private final int size;

    private T value = null;

    private Quad<T> topLeft, topRight, bottomLeft, bottomRight = null;

    public Quad(T value, Vector2i pos, int size) {
        this(null, 0, value, pos, size);
    }

    private Quad(Quad<T> parent, int greatestNodeDepth, T value, Vector2i pos, int size) {
        this.parent = parent;
        this.greatestNodeDepth = greatestNodeDepth;

        this.pos = new Vector2i(pos);
        this.size = size;

        this.value = value;
    }

    private void updateGreatestNodeDepth(boolean skip) {
        int previous = greatestNodeDepth;
        if (!skip) {
            int greatestNodeDepth1 = Math.max(topLeft.greatestNodeDepth, topRight.greatestNodeDepth);
            int greatestNodeDepth2 = Math.max(bottomLeft.greatestNodeDepth, bottomRight.greatestNodeDepth);
            greatestNodeDepth = Math.max(greatestNodeDepth1, greatestNodeDepth2);
        }

        if ((skip || previous != greatestNodeDepth) && parent != null)
            parent.updateGreatestNodeDepth(false);
    }

    public boolean isSubdivided() {
        return value == null;
    }

    public void subdivide(T topLeft, T topRight, T bottomLeft, T bottomRight) {
        if (isSubdivided())
            throw new IllegalCallerException("Can't subdivide a Quad that is already subdivided!");

        greatestNodeDepth++;
        updateGreatestNodeDepth(true);

        value = null;
        int halfSize = size/2;
        this.topLeft = new Quad<>(this, greatestNodeDepth, topLeft, new Vector2i(pos.x + halfSize, pos.y + halfSize), halfSize);
        this.topRight = new Quad<>(this, greatestNodeDepth, topRight, new Vector2i(pos.x, pos.y + halfSize), halfSize);
        this.bottomLeft = new Quad<>(this, greatestNodeDepth, bottomLeft, pos, halfSize);
        this.bottomRight = new Quad<>(this, greatestNodeDepth, bottomRight, new Vector2i(pos.x + halfSize, pos.y), halfSize);
    }

    public void unify(T value) {
        if (!isSubdivided())
            throw new IllegalCallerException("Can't unify a Quad that isn't subdivided!");

        greatestNodeDepth--;
        updateGreatestNodeDepth(true);

        this.value = value;
        topLeft = null;
        topRight = null;
        bottomLeft = null;
        bottomRight = null;
    }

    public ArrayList<Quad<T>> findDeepestNodes() {
        ArrayList<Quad<T>> nodes = new ArrayList<>(List.of(topLeft, topRight, bottomLeft, bottomRight));
        nodes.removeIf(node -> node.greatestNodeDepth < greatestNodeDepth);

        ArrayList<Quad<T>> deepestNodes = new ArrayList<>();
        for (Quad<T> node : nodes)
            deepestNodes.addAll(node.findDeepestNodes());


        for (Quad<T> node : deepestNodes)
            if (node.isSubdivided())
                throw new IllegalStateException("Leaf nodes cannot be subdivided!");


        return deepestNodes;
    }



    public Vector2i getPos() {
        return pos;
    }

    public int getSize() {
        return size;
    }

    public int getGreatestNodeDepth() {
        return greatestNodeDepth;
    }


    public T getValue() {
        return value;
    }

    public Quad<T> getTopLeft() {
        return topLeft;
    }

    public Quad<T> getTopRight() {
        return topRight;
    }

    public Quad<T> getBottomLeft() {
        return bottomLeft;
    }

    public Quad<T> getBottomRight() {
        return bottomRight;
    }
}
