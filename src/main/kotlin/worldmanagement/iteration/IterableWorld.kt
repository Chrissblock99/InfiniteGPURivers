package me.chriss99.worldmanagement.iteration;

import me.chriss99.IterationSurfaceType;
import me.chriss99.worldmanagement.InfiniteWorld;
import me.chriss99.worldmanagement.Region;
import me.chriss99.worldmanagement.TileLoadManager;
import org.joml.Vector2i;

public class IterableWorld extends InfiniteWorld<IterationTile> {
    public IterableWorld(String worldName, int chunkSize, int regionSize, TileLoadManager<Region<IterationTile>> tileLoadManager) {
        super(chunkSize, regionSize, (a, b) -> new IterationTile(0, 0, 0), new IterationTileRegionFileManager(worldName), tileLoadManager);
    }

    public IterationSurfaceType getIterationSurfaceType(Vector2i pos) {
        int v0 = getTile(pos).vertical & 0b11;
        int v1 = getTile(new Vector2i(pos).add(1, 0)).vertical & 0b11;
        int h0 = getTile(pos).horizontal & 0b11;
        int h1 = getTile(new Vector2i(pos).add(0, 1)).horizontal & 0b11;

        int combined = h1 | (v0 << 2) | (v1 << 4) | (h0 << 6);
        byte bits = (byte) switch (combined) {
            case 0b00000000 -> 0b0000;

            case 0b00111100 -> 0b0100;
            case 0b01000001 -> 0b0101;
            case 0b11000011 -> 0b0110;
            case 0b00010100 -> 0b0111;

            case 0b00110011 -> 0b1000;
            case 0b00001101 -> 0b1001;
            case 0b11010000 -> 0b1010;
            case 0b01000100 -> 0b1011;

            case 0b11001100 -> 0b1100;
            case 0b01110000 -> 0b1101;
            case 0b00000111 -> 0b1110;
            case 0b00010001 -> 0b1111;

            default -> throw new IllegalStateException("Illegal combination: " + combined);
        };

        return new IterationSurfaceType(bits);
    }
}
