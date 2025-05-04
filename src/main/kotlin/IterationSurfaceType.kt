package me.chriss99

import glm_.vec2.Vec2i
import java.util.*

class IterationSurfaceType(bits: Byte) {
    val bits: Byte = (bits.toInt() and 0x0F).toByte()
    val surfaceType: SurfaceType = SurfaceType.fromBits(bits)
    val direction: Vec2i = surfaceType.directionFromBits((bits.toInt() and 0b0011).toByte())

    val surface: Array<IntArray>
        get() {
            val s = surfaceType.surface
            return when (bits.toInt() and 0b0011) {
                0b00 -> s
                0b01 -> arrayOf(intArrayOf(s[1][0], s[0][0]), intArrayOf(s[1][1], s[0][1]))
                0b10 -> arrayOf(intArrayOf(s[0][1], s[1][1]), intArrayOf(s[0][0], s[1][0]))
                0b11 -> arrayOf(intArrayOf(s[1][1], s[1][0]), intArrayOf(s[0][1], s[0][0]))
                else -> throw IllegalStateException("Unexpected value: $bits")
            }
        }


    enum class SurfaceType(val directionFromBits: (direction: Byte) -> Vec2i, internal val surface: Array<IntArray>) {
        FLAT(::aaDirectionFromBits, Array(2) { IntArray(2) }),
        SLOPE(::aaDirectionFromBits, arrayOf(intArrayOf(0, 0), intArrayOf(1, 1))),
        OUTWARD_SLOPE(::diagonalDirectionFromBits, arrayOf(intArrayOf(0, 0), intArrayOf(0, 1))),
        INWARD_SLOPE(::diagonalDirectionFromBits, arrayOf(intArrayOf(0, 1), intArrayOf(1, 1)));

        companion object {
            fun fromBits(bits: Byte): SurfaceType {
                var bits = bits
                bits = (bits.toInt() and 0b1100).toByte()

                return when (bits) {
                    0b0000.toByte() -> FLAT
                    0b0100.toByte() -> SLOPE
                    0b1000.toByte() -> OUTWARD_SLOPE
                    0b1100.toByte() -> INWARD_SLOPE
                    else -> throw IllegalStateException("Unexpected value: $bits")
                }
            }
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as IterationSurfaceType
        return bits == that.bits
    }

    override fun hashCode(): Int {
        return Objects.hashCode(bits)
    }

    override fun toString(): String {
        return String.format("%4s", Integer.toBinaryString(bits.toInt() and 0xFF)).replace(' ', '0')
    }

    companion object {
        private fun aaDirectionFromBits(bits: Byte): Vec2i {
            return when (bits) {
                0b00.toByte() -> Vec2i(0, -1)
                0b01.toByte() -> Vec2i(1, 0)
                0b10.toByte() -> Vec2i(-1, 0)
                0b11.toByte() -> Vec2i(0, 1)
                else -> throw IllegalStateException("Unexpected value: $bits")
            }
        }

        private fun diagonalDirectionFromBits(bits: Byte): Vec2i {
            return when (bits) {
                0b00.toByte() -> Vec2i(-1, -1)
                0b01.toByte() -> Vec2i(1, -1)
                0b10.toByte() -> Vec2i(-1, 1)
                0b11.toByte() -> Vec2i(1, 1)
                else -> throw IllegalStateException("Unexpected value: $bits")
            }
        }
    }
}