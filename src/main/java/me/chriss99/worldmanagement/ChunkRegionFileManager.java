package me.chriss99.worldmanagement;

import me.chriss99.Array2DBufferWrapper;
import org.joml.Vector2i;

import java.nio.ByteBuffer;
import java.util.Map;

public class ChunkRegionFileManager implements RegionFileManager<Chunk> {
    private final FileLoadStoreManager<Region<Chunk>> fileManager;
    private final int chunkByteSize;
    private final int chunkDataByteSize;
    public final Array2DBufferWrapper.Type type;
    public final int chunkSize;

    public ChunkRegionFileManager(String worldName, Array2DBufferWrapper.Type type, int chunkSize) {
        this.fileManager = new FileLoadStoreManager<>("worlds/" + worldName, "region", this::regionFromByteArray, this::regionToByteArray);

        chunkDataByteSize = chunkSize*chunkSize*type.elementSize;
        chunkByteSize = chunkDataByteSize + 4*2;
        this.type = type;
        this.chunkSize = chunkSize;
    }

    public Region<Chunk> loadRegion(Vector2i regionCoord) {
        return fileManager.loadFile(regionCoord);
    }

    public void saveRegion(Region<Chunk> region) {
        fileManager.saveFile(region, region.coord);
    }

    private Region<Chunk> regionFromByteArray(byte[] array, Vector2i regionCoord) {
        int chunkNum = array.length/chunkByteSize;
        Region<Chunk> region = new Region<>(regionCoord);
        ByteBuffer buffer = ByteBuffer.wrap(array);

        for (int i = 0; i < chunkNum; i++) {
            Vector2i chunkCoord = new Vector2i(buffer.getInt(), buffer.getInt());
            byte[] byteArray = new byte[chunkDataByteSize];
            buffer.get(byteArray);
            Array2DBufferWrapper data = Array2DBufferWrapper.of(ByteBuffer.wrap(byteArray), type, chunkSize, chunkSize);
            region.addChunk(chunkCoord, new Chunk(data));
        }

        return region;
    }

    private byte[] regionToByteArray(Region<Chunk> region) {
        ByteBuffer buffer = ByteBuffer.allocate(region.getAllChunks().size()*chunkByteSize);

        for (Map.Entry<Vector2i, Chunk> entry : region.getAllChunks()) {
            buffer.putInt(entry.getKey().x);
            buffer.putInt(entry.getKey().y);
            buffer.put(entry.getValue().data().buffer);
        }

        return buffer.array();
    }
}
