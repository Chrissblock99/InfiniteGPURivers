package me.chriss99.worldmanagement.quadtree;

import me.chriss99.IterationSurfaceType;
import me.chriss99.worldmanagement.FileLoadStoreManager;
import org.joml.Vector2i;

import java.util.ArrayList;

public class QuadFileManager {
    private final FileLoadStoreManager<Quad<IterationSurfaceType>> fileManager;
    private final int quadTileSize;

    public QuadFileManager(String worldName, int quadTileSize) {
        fileManager = new FileLoadStoreManager<>("worlds/" + worldName, "quadtree", this::quadFromBytes, QuadFileManager::quadToBytes);
        this.quadTileSize = quadTileSize;
    }

    public Quad<IterationSurfaceType> loadQuad(Vector2i chunkCoord) {
        return fileManager.loadFile(chunkCoord);
    }

    public void saveQuad(Quad<IterationSurfaceType> quad) {
        fileManager.saveFile(quad, quad.getPos());
    }

    private static byte[] quadToBytes(Quad<IterationSurfaceType> quad) {
        ArrayList<Boolean> treeBits = new ArrayList<>();
        ArrayList<IterationSurfaceType> values = new ArrayList<>();
        flatten(quad, treeBits, values);
        return toArray(treeBits, values);
    }

    private static void flatten(Quad<IterationSurfaceType> quad, ArrayList<Boolean> treeBits, ArrayList<IterationSurfaceType> values) {
        boolean hasValue = !quad.isSubdivided();
        treeBits.add(hasValue);

        if (hasValue)
            values.add(quad.getValue());
        else {
            flatten(quad.getTopLeft(), treeBits, values);
            flatten(quad.getTopRight(), treeBits, values);
            flatten(quad.getBottomLeft(), treeBits, values);
            flatten(quad.getBottomRight(), treeBits, values);
        }
    }

    private static byte[] toArray(ArrayList<Boolean> treeBits, ArrayList<IterationSurfaceType> values) {
        int treeBitsBytes = (int) Math.ceil(((double) treeBits.size())/8d);
        int valuesBytes = (int) Math.ceil(((double) treeBits.size())/2d);
        byte[] bytes = new byte[treeBitsBytes + valuesBytes];

        for (int i = 0; i < treeBits.size(); i++) {
            if (!treeBits.get(i))
                continue;

            int index = i/8;
            int offset = i%8;
            bytes[index] |= (byte) (0b10000000 >>> offset);
        }

        for (int i = 0; i < values.size(); i++) {
            int index = i/2;
            boolean offset = i%2 == 0;
            byte bits = values.get(i).toBits();
            bytes[treeBitsBytes + index] |= offset ? (byte) (bits << 4) : bits;
        }

        return bytes;
    }

    private record TreeData(ArrayList<Boolean> treeBits, int valuesNum) {}

    private Quad<IterationSurfaceType> quadFromBytes(byte[] bytes, Vector2i pos) {
        TreeData treeData = treeData(bytes);
        ArrayList<Boolean> treeBits = treeData.treeBits;
        int valuesNum = treeData.valuesNum;

        int treeBitsBytes = (int) Math.ceil(((double) treeBits.size())/8d);
        ArrayList<IterationSurfaceType> values = values(bytes, treeBitsBytes, valuesNum);

        return buildTree(treeBits, values, pos, quadTileSize);
    }

    private static TreeData treeData(byte[] bytes) {
        ArrayList<Boolean> treeBits = new ArrayList<>();
        int valuesNum = 0;

        int remainingBits = 1;
        for (int i = 0; remainingBits != 0; i++) {
            remainingBits--;

            int index = i/8;
            int offset = i%8;
            boolean hasValue = ((0b10000000 >>> offset) & bytes[index]) != 0;

            treeBits.add(hasValue);

            if (hasValue)
                valuesNum++;
            else
                remainingBits += 4;
        }

        return new TreeData(treeBits, valuesNum);
    }

    private static ArrayList<IterationSurfaceType> values(byte[] bytes, int treeBitsBytes, int valuesNum) {
        ArrayList<IterationSurfaceType> values = new ArrayList<>();

        for (int i = 0; i < valuesNum; i++) {
            byte bits = bytes[treeBitsBytes + i/2];
            boolean offset = i%2 == 0;

            bits = (byte) (offset ? (bits >>> 4) : (0x0F & bits));
            values.add(new IterationSurfaceType(bits));
        }

        return values;
    }

    private static Quad<IterationSurfaceType> buildTree(ArrayList<Boolean> treeBits, ArrayList<IterationSurfaceType> values, Vector2i pos, int size) {
        return Quad.depthFirstInstance(() -> {
            boolean hasValue = treeBits.get(0);
            treeBits.remove(0);

            if (hasValue){
                IterationSurfaceType surfaceType = values.get(0);
                values.remove(0);
                return surfaceType;
            } else
                return null;
        }, pos, size);
    }
}
