package me.chriss99

import glm_.vec2.Vec2i

data class Area(val srcPos: Vec2i, val endPos: Vec2i) {
    val size: Vec2i get() = endPos - srcPos
    val width: Int get() = endPos.x - srcPos.x
    val height: Int get() = endPos.y - srcPos.y
    val area: Int get() = width * height
    val innerPoints: Array<Vec2i> get() = Array(area) { srcPos + Vec2i(it / height, it % height) }

    init {
        if (!validArea(srcPos, endPos))
            throw IllegalArgumentException("Src and endPos do not form a valid area! srcPos: $srcPos, endPos: $endPos")
    }

    constructor() : this(0)
    constructor(size: Int) : this(Vec2i(), size)
    constructor(size: Vec2i) : this(Vec2i(), size)
    constructor(srcPos: Vec2i, size: Int) : this(srcPos, srcPos + Vec2i(size))

    private fun validArea(srcPos: Vec2i, endPos: Vec2i): Boolean = srcPos.allLessThanEqual(endPos)

    operator fun contains(area: Area): Boolean = area.srcPos.allGreaterThanEqual(srcPos) && area.endPos.allLessThanEqual(endPos)
    operator fun contains(pos: Vec2i): Boolean = pos.allGreaterThanEqual(srcPos) && pos.allLessThan(endPos)

    infix fun intersect(area: Area): Area? {
        val srcPos = Vec2i(srcPos.x.coerceAtLeast(area.srcPos.x), srcPos.y.coerceAtLeast(area.srcPos.y))
        val endPos = Vec2i(endPos.x.coerceAtMost(area.endPos.x), endPos.y.coerceAtMost(area.endPos.y))

        return if (validArea(srcPos, endPos))
            Area(srcPos, endPos)
        else null
    }

    fun outset(distance: Int): Area = increase(distance, distance, distance, distance)
    fun increase(right: Int, up: Int, left: Int, down: Int): Area = Area(srcPos - Vec2i(left, down), endPos + Vec2i(right, up))

    operator fun plus(shift: Vec2i): Area = Area(srcPos + shift, endPos + shift)
    operator fun minus(shift: Vec2i): Area = Area(srcPos - shift, endPos - shift)

    operator fun times(scalar: Int): Area = Area(srcPos * scalar, endPos * scalar)
    operator fun div(scalar: Int): Area = Area(srcPos / scalar, endPos / scalar)
}