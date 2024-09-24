package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Map;

public class RegionFileManager<T> {
    private final String worldName;
    private final int chunkByteSize;
    private final BufferInterpreter<T> bufferInterpreter;
    private final Class<T> tClass;

    public RegionFileManager(String worldName, BufferInterpreter<T> bufferInterpreter) {
        this.worldName = worldName;
        chunkByteSize = 2*4 + bufferInterpreter.byteSize()*100*100;

        this.bufferInterpreter = bufferInterpreter;
        this.tClass = bufferInterpreter.typeClass();
        new File("worlds/" + worldName).mkdirs();
    }

    public Region<T> loadRegion(Vector2i regionCoord) {
        File file = getRegionFile(regionCoord);

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            throw new RuntimeException(e1);
        }

        try {
            return RegionFromByteArray(inputStream.readAllBytes(), regionCoord);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveRegion(Region<T> region) {
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

    private Region<T> RegionFromByteArray(byte[] array, Vector2i regionCoord) throws IOException, ClassNotFoundException {
        int chunkNum = array.length/chunkByteSize;
        Region<T> region = new Region<>(regionCoord);
        ByteBuffer buffer = ByteBuffer.wrap(array);

        for (int i = 0; i < chunkNum; i++) {
            Vector2i chunkCoord = new Vector2i(buffer.getInt(), buffer.getInt());
            @SuppressWarnings("unchecked")
            T[][] data = (T[][]) Array.newInstance(tClass, 100, 100);

            for (int x = 0; x < 100; x++)
                for (int y = 0; y < 100; y++)
                    data[x][y] = bufferInterpreter.getFromByteBuffer(buffer);

            region.addChunk(chunkCoord, new Chunk<>(data));
        }

        return region;
    }

    private byte[] RegionToByteArray(Region<T> region) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(region.getAllChunks().size()*chunkByteSize);

        for (Map.Entry<Vector2i, Chunk<T>> entry : region.getAllChunks()) {
            buffer.putInt(entry.getKey().x);
            buffer.putInt(entry.getKey().y);
            T[][] data = entry.getValue().data();

            for (int x = 0; x < 100; x++)
                for (int y = 0; y < 100; y++)
                    bufferInterpreter.putInByteBuffer(buffer, data[x][y]);
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
