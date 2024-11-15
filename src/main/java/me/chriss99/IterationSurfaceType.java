package me.chriss99;

import org.joml.Vector2i;

import java.util.Objects;
import java.util.function.Function;

public class IterationSurfaceType {
    private final SurfaceType surfaceType;
    private final Vector2i direction;
    private final byte bits;

    public IterationSurfaceType(byte bits) {
        surfaceType = SurfaceType.fromBits(bits);
        this.direction = surfaceType.directionFromBits.apply((byte) (bits & 0b0011));
        this.bits = (byte) (bits & 0x0F);
    }

    public SurfaceType getSurfaceType() {
        return surfaceType;
    }

    public Vector2i getDirection() {
        return new Vector2i(direction);
    }

    public byte toBits() {
        return bits;
    }

    public int[][] getSurface() {
        int[][] s = surfaceType.surface;
        return switch (bits & 0b0011) {
            case 0b00 -> s;
            case 0b01 -> new int[][]{{s[1][0], s[0][0]}, {s[1][1], s[0][1]}};
            case 0b10 -> new int[][]{{s[0][1], s[1][1]}, {s[0][0], s[1][0]}};
            case 0b11 -> new int[][]{{s[1][1], s[1][0]}, {s[0][1], s[0][0]}};
            default -> throw new IllegalStateException("Unexpected value: " + bits);
        };
    }




    private static Vector2i aaDirectionFromBits(byte bits) {
        return switch (bits) {
            case 0b00 -> new Vector2i(0, -1);
            case 0b01 -> new Vector2i(1, 0);
            case 0b10 -> new Vector2i(-1, 0);
            case 0b11 -> new Vector2i(0, 1);
            default -> throw new IllegalStateException("Unexpected value: " + bits);
        };
    }

    private static Vector2i diagonalDirectionFromBits(byte bits) {
        return switch (bits) {
            case 0b00 -> new Vector2i(-1, -1);
            case 0b01 -> new Vector2i(1, -1);
            case 0b10 -> new Vector2i(-1, 1);
            case 0b11 -> new Vector2i(1, 1);
            default -> throw new IllegalStateException("Unexpected value: " + bits);
        };
    }


    public enum SurfaceType {
        FLAT(IterationSurfaceType::aaDirectionFromBits, new int[2][2]),
        SLOPE(IterationSurfaceType::aaDirectionFromBits, new int[][]{{0, 0}, {1, 1}}),
        OUTWARD_SLOPE(IterationSurfaceType::diagonalDirectionFromBits, new int[][]{{0, 0}, {0, 1}}),
        INWARD_SLOPE(IterationSurfaceType::diagonalDirectionFromBits, new int[][]{{0, 1}, {1, 1}});

        private final Function<Byte, Vector2i> directionFromBits;
        private final int[][] surface;

        SurfaceType(Function<Byte, Vector2i> directionFromBits, int[][] surface) {
            this.directionFromBits = directionFromBits;
            this.surface = surface;
        }


        private static SurfaceType fromBits(byte bits) {
            bits &= 0b1100;

            return switch (bits) {
                case 0b0000 -> FLAT;
                case 0b0100 -> SLOPE;
                case 0b1000 -> OUTWARD_SLOPE;
                case 0b1100 -> INWARD_SLOPE;
                default -> throw new IllegalStateException("Unexpected value: " + bits);
            };
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IterationSurfaceType that = (IterationSurfaceType) o;
        return bits == that.bits;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bits);
    }

    @Override
    public String toString() {
        return String.format("%4s", Integer.toBinaryString(bits & 0xFF)).replace(' ', '0');
    }
}
