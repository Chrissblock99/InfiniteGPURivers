package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map;

public class RegionFileManager {
    private final String worldName;
    private static final int chunkByteSize = 4*(2+100*100);

    public RegionFileManager(String worldName) {
        this.worldName = worldName;
        new File("worlds/" + worldName).mkdirs();
    }

    public Region loadRegion(Vector2i regionCoord) {
        File file = getRegionFile(regionCoord);

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            throw new RuntimeException(e1);
        }

        try {
            return RegionFromByteArray(inputStream.readAllBytes(), regionCoord);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveRegion(Region region) {
        File file = getRegionFile(region.coord);

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e1) {
            throw new RuntimeException(e1);
        }

        try {
            outputStream.write(RegionToByteArray(region));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Region RegionFromByteArray(byte[] array, Vector2i regionCoord) throws IOException {
        int chunkNum = array.length/chunkByteSize;
        Region region = new Region(regionCoord);
        ByteBuffer buffer = ByteBuffer.wrap(array);

        for (int i = 0; i < chunkNum; i++) {
            Vector2i chunkCoord = new Vector2i(buffer.getInt(), buffer.getInt());
            float[][] data = new float[100][100];

            for (int x = 0; x < 100; x++)
                for (int y = 0; y < 100; y++)
                    data[x][y] = buffer.getFloat();

            //region.addChunk(chunkCoord, new Chunk(data));
        }

        return region;
    }

    private byte[] RegionToByteArray(Region region) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(region.getAllChunks().size()*chunkByteSize);

        for (Map.Entry<Vector2i, Chunk> entry : region.getAllChunks()) {
            buffer.putInt(entry.getKey().x);
            buffer.putInt(entry.getKey().y);
            /*float[][] data = entry.getValue().data();

            for (int x = 0; x < 100; x++)
                for (int y = 0; y < 100; y++)
                    buffer.putFloat(data[x][y]);*/
        }

        return buffer.array();
    }

    private File getRegionFile(Vector2i coord) {
        File file = new File("worlds/" + worldName + "/" + coord.x + ";" + coord.y + ".region");

        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e1) {
                throw new RuntimeException("Could not write to the region folder!");
            }

        return file;
    }
}
