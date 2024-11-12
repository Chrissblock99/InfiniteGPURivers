package me.chriss99.worldmanagement;

import me.chriss99.Array2DBufferWrapper;
import org.joml.Vector2i;

import java.nio.ByteBuffer;
import java.util.Map;

public class RegionFileManager {
    private final FileLoadStoreManager<Region> fileManager;
    private final int chunkByteSize;
    private final int chunkDataByteSize;
    public final int format;
    public final int type;
    public final int chunkSize;

    public RegionFileManager(String worldName, int format, int type, int chunkSize) {
        this.fileManager = new FileLoadStoreManager<>("worlds/" + worldName, "region", this::regionFromByteArray, this::regionToByteArray);

        chunkDataByteSize = chunkSize*chunkSize*Array2DBufferWrapper.sizeOf(format, type);
        chunkByteSize = chunkDataByteSize + 4*2;
        this.format = format;
        this.type = type;
        this.chunkSize = chunkSize;
    }

    public Region loadRegion(Vector2i regionCoord) {
        return fileManager.loadFile(regionCoord);
    }

    public void saveRegion(Region region) {
        fileManager.saveFile(region, region.coord);
    }

    private Region regionFromByteArray(byte[] array, Vector2i regionCoord) {
        int chunkNum = array.length/chunkByteSize;
        Region region = new Region(regionCoord);
        ByteBuffer buffer = ByteBuffer.wrap(array);

        for (int i = 0; i < chunkNum; i++) {
            Vector2i chunkCoord = new Vector2i(buffer.getInt(), buffer.getInt());
            byte[] byteArray = new byte[chunkDataByteSize];
            buffer.get(byteArray);
            Array2DBufferWrapper data = new Array2DBufferWrapper(ByteBuffer.wrap(byteArray), format, type, chunkSize, chunkSize);
            region.addChunk(chunkCoord, new Chunk(data));
        }

        return region;
    }

    private byte[] regionToByteArray(Region region) {
        ByteBuffer buffer = ByteBuffer.allocate(region.getAllChunks().size()*chunkByteSize);

        for (Map.Entry<Vector2i, Chunk> entry : region.getAllChunks()) {
            buffer.putInt(entry.getKey().x);
            buffer.putInt(entry.getKey().y);
            buffer.put(entry.getValue().data().buffer);
        }

        return buffer.array();
    }
}
