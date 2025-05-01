package me.chriss99

import glm_.vec2.Vec2i
import java.util.function.Consumer

class Area(srcPos: Vec2i, endPos: Vec2i) {
    @JvmOverloads
    constructor(size: Int = 0) : this(Vec2i(size))

    constructor(size: Vec2i) : this(Vec2i(), size)

    constructor(srcPos: Vec2i, size: Int) : this(srcPos, Vec2i(size).plus(srcPos))

    fun contains(area: Area): Boolean {
        return area.srcPos.x >= srcPos.x && area.srcPos.y >= srcPos.y && area.endPos.x <= endPos.x && area.endPos.y <= endPos.y
    }

    fun contains(pos: Vec2i): Boolean {
        return pos.x >= srcPos.x && pos.y >= srcPos.y && pos.x < endPos.x && pos.y < endPos.y
    }

    fun intersection(area: Area): Area? {
        val srcPos: Vec2i = Vec2i(
            Math.max(srcPos.x, area.srcPos.x), Math.max(
                srcPos.y, area.srcPos.y
            )
        )
        val endPos: Vec2i = Vec2i(
            Math.min(endPos.x, area.endPos.x), Math.min(
                endPos.y, area.endPos.y
            )
        )

        if (validArea(srcPos, endPos)) return Area(srcPos, endPos)
        return null
    }

    fun outset(distance: Int): Area {
        return increase(distance, distance, distance, distance)
    }

    fun increase(right: Int, up: Int, left: Int, down: Int): Area {
        return Area(Vec2i(srcPos).minus(left, down), Vec2i(endPos).plus(right, up))
    }

    fun plus(shift: Vec2i): Area {
        return Area(Vec2i(srcPos).plus(shift), Vec2i(endPos).plus(shift))
    }

    fun minus(shift: Vec2i): Area {
        return Area(Vec2i(srcPos).minus(shift), Vec2i(endPos).minus(shift))
    }

    fun times(scalar: Int): Area {
        return Area(Vec2i(srcPos).times(scalar), Vec2i(endPos).times(scalar))
    }

    fun div(scalar: Int): Area {
        return Area(Vec2i(srcPos).div(scalar), Vec2i(endPos).div(scalar))
    }


    fun allPoints(): Array<Vec2i> {
        val points: Array<Vec2i?> = arrayOfNulls(area)

        for (x in 0..<width)
            for (y in 0..<height)
                points[x * height + y] = Vec2i(srcPos).plus(x, y)

        return points as Array<Vec2i>
    }

    fun forAllPoints(consumer: Consumer<Vec2i>) {
        for (pos in allPoints())
            consumer.accept(pos)
    }

    val size: Vec2i
        get() = Vec2i(endPos).minus(srcPos)

    val width: Int
        get() = endPos.x - srcPos.x

    val height: Int
        get() = endPos.y - srcPos.y

    val area: Int
        get() = width * height

    fun srcPos(): Vec2i {
        return Vec2i(srcPos)
    }

    fun endPos(): Vec2i {
        return Vec2i(endPos)
    }

    fun copy(): Area {
        return Area(srcPos, endPos)
    }

    val srcPos: Vec2i
    val endPos: Vec2i

    init {
        require(
            validArea(
                srcPos,
                endPos
            )
        ) { "Src and endPos do not form a valid area! srcPos: $srcPos endPos: $endPos" }

        this.srcPos = Vec2i(srcPos)
        this.endPos = Vec2i(endPos)
    }

    companion object {
        private fun validArea(srcPos: Vec2i, endPos: Vec2i): Boolean {
            return srcPos.x <= endPos.x && srcPos.y <= endPos.y
        }
    }
}