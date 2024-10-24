package me.chriss99;

import org.joml.Vector2i;

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
        FLAT(IterationSurfaceType::aaDirectionFromBits),
        SLOPE(IterationSurfaceType::aaDirectionFromBits),
        OUTWARD_SLOPE(IterationSurfaceType::diagonalDirectionFromBits),
        INWARD_SLOPE(IterationSurfaceType::diagonalDirectionFromBits);

        private final Function<Byte, Vector2i> directionFromBits;

        SurfaceType(Function<Byte, Vector2i> directionFromBits) {
            this.directionFromBits = directionFromBits;
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
}
