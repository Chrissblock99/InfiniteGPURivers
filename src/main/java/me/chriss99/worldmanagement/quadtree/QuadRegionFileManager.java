package me.chriss99.worldmanagement.quadtree;

import me.chriss99.IterationSurfaceType;
import me.chriss99.worldmanagement.FileLoadStoreManager;
import me.chriss99.worldmanagement.Region;
import me.chriss99.worldmanagement.RegionFileManager;
import org.joml.Vector2i;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class QuadRegionFileManager implements RegionFileManager<Quad<IterationSurfaceType>> {
    private final FileLoadStoreManager<Region<Quad<IterationSurfaceType>>> fileManager;
    private final int quadTileSize;

    public QuadRegionFileManager(String worldName, int quadTileSize) {
        fileManager = new FileLoadStoreManager<>("worlds/" + worldName, "quadtree", this::quadRegionFromBytes, QuadRegionFileManager::quadRegionToBytes);
        this.quadTileSize = quadTileSize;
    }

    public Region<Quad<IterationSurfaceType>> loadRegion(Vector2i chunkCoord) {
        return fileManager.loadFile(chunkCoord);
    }

    public void saveRegion(Region<Quad<IterationSurfaceType>> quadRegion) {
        fileManager.saveFile(quadRegion, quadRegion.coord);
    }

    private static byte[] quadRegionToBytes(Region<Quad<IterationSurfaceType>> quadRegion) {
        LinkedList<byte[]> bytesList = new LinkedList<>();
        int length = 0;

        for (Quad<IterationSurfaceType> quad : quadRegion.getAllChunks().stream().map(Map.Entry::getValue).toList()) {
            ArrayList<Boolean> treeBits = new ArrayList<>();
            ArrayList<IterationSurfaceType> values = new ArrayList<>();
            flatten(quad, treeBits, values);
            byte[] bytes = toArray(quad.getPos(), treeBits, values);

            bytesList.add(bytes);
            length += bytes.length;
        }

        byte[] fullBytes = new byte[length];
        int offset = 0;
        for (byte[] bytes : bytesList) {
            System.arraycopy(bytes, 0, fullBytes, offset, bytes.length);
            offset += bytes.length;
        }

        return fullBytes;
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

    private static byte[] toArray(Vector2i pos, ArrayList<Boolean> treeBits, ArrayList<IterationSurfaceType> values) {
        int treeBitsBytes = (int) Math.ceil(((double) treeBits.size())/8d);
        int valuesBytes = (int) Math.ceil(((double) values.size())/2d);
        byte[] bytes = new byte[8 + treeBitsBytes + valuesBytes];

        byte[] posBytes = ByteBuffer.allocate(8).putInt(pos.x).putInt(pos.y).array();
        System.arraycopy(posBytes, 0, bytes, 0, 8);

        for (int i = 0; i < treeBits.size(); i++) {
            if (!treeBits.get(i))
                continue;

            int index = i/8;
            int offset = i%8;
            bytes[8 + index] |= (byte) (0b10000000 >>> offset);
        }

        for (int i = 0; i < values.size(); i++) {
            int index = i/2;
            boolean offset = i%2 == 0;
            byte bits = values.get(i).toBits();
            bytes[8 + treeBitsBytes + index] |= offset ? (byte) (bits << 4) : bits;
        }

        return bytes;
    }

    private record TreeData(ArrayList<Boolean> treeBits, int valuesNum) {}

    private Region<Quad<IterationSurfaceType>> quadRegionFromBytes(byte[] bytes, Vector2i pos) {
        Region<Quad<IterationSurfaceType>> quadRegion = new Region<>(pos);
        int readOffset = 0;

        while (readOffset < bytes.length) {
            byte[] posBytes = new byte[8];
            System.arraycopy(bytes, readOffset, posBytes, 0, 8);
            ByteBuffer posBuffer = ByteBuffer.wrap(posBytes);
            Vector2i quadPos = new Vector2i(posBuffer.getInt(), posBuffer.getInt());

            TreeData treeData = treeData(bytes, readOffset + 8);
            ArrayList<Boolean> treeBits = treeData.treeBits;
            int valuesNum = treeData.valuesNum;

            int treeBitsBytes = (int) Math.ceil(((double) treeBits.size()) / 8d);
            ArrayList<IterationSurfaceType> values = values(bytes, readOffset + 8, treeBitsBytes, valuesNum);
            readOffset += 8 + treeBitsBytes + (int) Math.ceil(((double) valuesNum) / 2d);

            quadRegion.addChunk(quadPos, buildTree(treeBits, values, quadPos, quadTileSize));
        }

        return quadRegion;
    }

    private static TreeData treeData(byte[] bytes, int readOffset) {
        ArrayList<Boolean> treeBits = new ArrayList<>();
        int valuesNum = 0;

        int remainingBits = 1;
        for (int i = readOffset*8; remainingBits != 0; i++) {
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

    private static ArrayList<IterationSurfaceType> values(byte[] bytes, int readOffset, int treeBitsBytes, int valuesNum) {
        ArrayList<IterationSurfaceType> values = new ArrayList<>();

        for (int i = 0; i < valuesNum; i++) {
            byte bits = bytes[readOffset + treeBitsBytes + i/2];
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
