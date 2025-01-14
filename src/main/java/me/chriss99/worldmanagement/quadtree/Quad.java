package me.chriss99.worldmanagement.quadtree;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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


    /**
     * Instances a generic Quadtree using a supplier called in depth first order <br>
     * The passed supplier dictates when to subdivide by returning null <br>
     * When the current node is a leaf the supplier should return a not null value
     *
     * @param depthFirst returns null when the current node should be subdivided and a value when it is a leaf
     * @param pos the position of the Quad
     * @param size the size of the Quad
     * @return The fully built Quadtree
     * @param <T> the generic type of the Quadtree
     */
    public static <T> Quad<T> depthFirstInstance(Supplier<T> depthFirst, Vector2i pos, int size) {
        ArrayList<Quad<T>> leafs = new ArrayList<>();
        Quad<T> quad = recursiveInstance(leafs, null, depthFirst, 0, pos, size);
        leafs.forEach(a -> a.updateGreatestNodeDepth(true));
        return quad;
    }

    private static <T> Quad<T> recursiveInstance(ArrayList<Quad<T>> leafs, Quad<T> parent, Supplier<T> depthFirst, int nodeDepth, Vector2i pos, int size) {
        T value = depthFirst.get();
        Quad<T> node = new Quad<>(parent, nodeDepth, value, pos, size);

        if (value != null) {
            leafs.add(node);
            return node;
        }

        nodeDepth++;

        int halfSize = size/2;
        node.topLeft = recursiveInstance(leafs, node, depthFirst, nodeDepth, new Vector2i(pos.x, pos.y + halfSize), halfSize);
        node.topRight = recursiveInstance(leafs, node, depthFirst, nodeDepth, new Vector2i(pos.x + halfSize, pos.y + halfSize), halfSize);
        node.bottomLeft = recursiveInstance(leafs, node, depthFirst, nodeDepth, new Vector2i(pos), halfSize);
        node.bottomRight = recursiveInstance(leafs, node, depthFirst, nodeDepth, new Vector2i(pos.x + halfSize, pos.y), halfSize);

        return node;
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
        this.topLeft = new Quad<>(this, greatestNodeDepth, topLeft, new Vector2i(pos.x, pos.y + halfSize), halfSize);
        this.topRight = new Quad<>(this, greatestNodeDepth, topRight, new Vector2i(pos.x + halfSize, pos.y + halfSize), halfSize);
        this.bottomLeft = new Quad<>(this, greatestNodeDepth, bottomLeft, new Vector2i(pos), halfSize);
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

    public void iterateAllLeafs(Consumer<Quad<T>> function) {
        if (value != null) {
            function.accept(this);
            return;
        }

        topLeft.iterateAllLeafs(function);
        topRight.iterateAllLeafs(function);
        bottomLeft.iterateAllLeafs(function);
        bottomRight.iterateAllLeafs(function);
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
        return new Vector2i(pos);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quad<?> quad = (Quad<?>) o;
        return greatestNodeDepth == quad.greatestNodeDepth && size == quad.size && Objects.equals(pos, quad.pos) && Objects.equals(value, quad.value) && Objects.equals(topLeft, quad.topLeft) && Objects.equals(topRight, quad.topRight) && Objects.equals(bottomLeft, quad.bottomLeft) && Objects.equals(bottomRight, quad.bottomRight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(greatestNodeDepth, pos, size, value, topLeft, topRight, bottomLeft, bottomRight);
    }

    @Override
    public String toString() {
        return "Quad{" +
                "greatestNodeDepth=" + greatestNodeDepth +
                ", pos=" + pos +
                ", size=" + size +
                ((!isSubdivided()) ?
                        ", value=" + value
                        :
                        ", topLeft=" + topLeft +
                        ", topRight=" + topRight +
                        ", bottomLeft=" + bottomLeft +
                        ", bottomRight=" + bottomRight) +
                '}';
    }
}
