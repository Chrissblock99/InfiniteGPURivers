package me.chriss99.worldmanagement.iteration;

import me.chriss99.worldmanagement.AbstractRegionFileManager;
import me.chriss99.worldmanagement.Region;
import org.joml.Vector2i;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public class IterationTileRegionFileManager extends AbstractRegionFileManager<IterationTile> {
    public IterationTileRegionFileManager(String worldName) {
        super(worldName);
    }

    @Override
    protected Region<IterationTile> regionFromBytes(byte[] bytes, Vector2i pos) {
        Region<IterationTile> region = new Region<>(pos);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        while (buffer.hasRemaining()) {
            Vector2i tilePos = new Vector2i(buffer.getInt(), buffer.getInt());
            int iteration = buffer.getInt();
            byte bits = buffer.get();

            int horizontal = switch (bits & 0b11) {
                case 0b00 -> 0;
                case 0b01 -> 1;
                case 0b11 -> -1;
                default -> throw new IllegalStateException("Unexpected bit pattern: " + bits);
            };
            int vertical = switch ((bits >>> 2) & 0b11) {
                case 0b00 -> 0;
                case 0b01 -> 1;
                case 0b11 -> -1;
                default -> throw new IllegalStateException("Unexpected bit pattern: " + bits);
            };

            region.addChunk(tilePos, new IterationTile(horizontal, vertical, iteration));
        }

        return region;
    }

    @Override
    protected byte[] regionToBytes(Region<IterationTile> region) {
        Set<Map.Entry<Vector2i, IterationTile>> tileEntrySet = region.getAllTiles();
        byte[] array = new byte[tileEntrySet.size() * (4 + 4 + 4 + 1)];
        ByteBuffer buffer = ByteBuffer.wrap(array);

        for (Map.Entry<Vector2i, IterationTile> entry : tileEntrySet) {
            buffer.putInt(entry.getKey().x).putInt(entry.getKey().y);
            IterationTile tile = entry.getValue();
            buffer.putInt(tile.iteration);
            buffer.put((byte) ((tile.horizontal << 2) | (tile.vertical & 0b11)));
        }

        return array;
    }
}
