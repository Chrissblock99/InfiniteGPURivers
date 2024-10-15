package me.chriss99;

import org.joml.Vector2i;

import java.util.function.Function;

public class IterationSurfaceType {
    private final SurfaceType surfaceType;
    private final Vector2i direction;

    public IterationSurfaceType(byte bits) {
        surfaceType = SurfaceType.fromBits(bits);
        this.direction = surfaceType.directionFromBits.apply((byte) (bits & 0b0011));
    }

    public SurfaceType getSurfaceType() {
        return surfaceType;
    }

    public Vector2i getDirection() {
        return new Vector2i(direction);
    }

    public byte toBits() {
        return surfaceType.toBits.apply(direction);
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

    private static byte bitsFromAADirection(Vector2i direction) {
        int data = ((direction.x << 4) & 0xF0) | (direction.y & 0x0F);
        return switch (data) {
            case 0x0F -> 0b00;
            case 0x10 -> 0b01;
            case 0xF0 -> 0b10;
            case 0x01 -> 0b11;
            default -> throw new IllegalStateException("Unexpected value: " + direction);
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

    private static byte bitsFromDiagonalDirection(Vector2i direction) {
        int data = ((direction.x << 4) & 0xF0) | (direction.y & 0x0F);
        return switch (data) {
            case 0xFF -> 0b00;
            case 0x1F -> 0b01;
            case 0xF1 -> 0b10;
            case 0x11 -> 0b11;
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
    }


    public enum SurfaceType {
        FLAT(IterationSurfaceType::aaDirectionFromBits, IterationSurfaceType::bitsFromAADirection),
        SLOPE(IterationSurfaceType::aaDirectionFromBits, vector2i -> (byte) (0b0100 | bitsFromAADirection(vector2i))),
        OUTWARD_SLOPE(IterationSurfaceType::diagonalDirectionFromBits, vector2i -> (byte) (0b1000 | bitsFromDiagonalDirection(vector2i))),
        INWARD_SLOPE(IterationSurfaceType::diagonalDirectionFromBits, vector2i -> (byte) (0b1100 | bitsFromDiagonalDirection(vector2i)));

        private final Function<Byte, Vector2i> directionFromBits;
        private final Function<Vector2i, Byte> toBits;

        SurfaceType(Function<Byte, Vector2i> directionFromBits, Function<Vector2i, Byte> toBits) {
            this.directionFromBits = directionFromBits;
            this.toBits = toBits;
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
