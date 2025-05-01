package me.chriss99

import org.joml.Vector2i
import java.util.function.Consumer

class Area(srcPos: Vector2i, endPos: Vector2i) {
    @JvmOverloads
    constructor(size: Int = 0) : this(Vector2i(size))

    constructor(size: Vector2i) : this(Vector2i(), size)

    constructor(srcPos: Vector2i, size: Int) : this(srcPos, Vector2i(size).add(srcPos))

    fun contains(area: Area): Boolean {
        return area.srcPos.x >= srcPos.x && area.srcPos.y >= srcPos.y && area.endPos.x <= endPos.x && area.endPos.y <= endPos.y
    }

    fun contains(pos: Vector2i): Boolean {
        return pos.x >= srcPos.x && pos.y >= srcPos.y && pos.x < endPos.x && pos.y < endPos.y
    }

    fun intersection(area: Area): Area? {
        val srcPos: Vector2i = Vector2i(
            Math.max(srcPos.x, area.srcPos.x), Math.max(
                srcPos.y, area.srcPos.y
            )
        )
        val endPos: Vector2i = Vector2i(
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
        return Area(Vector2i(srcPos).sub(left, down), Vector2i(endPos).add(right, up))
    }

    fun add(shift: Vector2i?): Area {
        return Area(Vector2i(srcPos).add(shift), Vector2i(endPos).add(shift))
    }

    fun sub(shift: Vector2i?): Area {
        return Area(Vector2i(srcPos).sub(shift), Vector2i(endPos).sub(shift))
    }

    fun mul(scalar: Int): Area {
        return Area(Vector2i(srcPos).mul(scalar), Vector2i(endPos).mul(scalar))
    }

    fun div(scalar: Int): Area {
        return Area(Vector2i(srcPos).div(scalar), Vector2i(endPos).div(scalar))
    }


    fun allPoints(): Array<Vector2i> {
        val points: Array<Vector2i?> = arrayOfNulls(area)

        for (x in 0..<width)
            for (y in 0..<height)
                points[x * height + y] = Vector2i(srcPos).add(x, y)

        return points as Array<Vector2i>
    }

    fun forAllPoints(consumer: Consumer<Vector2i>) {
        for (pos in allPoints())
            consumer.accept(pos)
    }

    val size: Vector2i
        get() = Vector2i(endPos).sub(srcPos)

    val width: Int
        get() = endPos.x - srcPos.x

    val height: Int
        get() = endPos.y - srcPos.y

    val area: Int
        get() = width * height

    fun srcPos(): Vector2i {
        return Vector2i(srcPos)
    }

    fun endPos(): Vector2i {
        return Vector2i(endPos)
    }

    fun copy(): Area {
        return Area(srcPos, endPos)
    }

    val srcPos: Vector2i
    val endPos: Vector2i

    init {
        require(
            validArea(
                srcPos,
                endPos
            )
        ) { "Src and endPos do not form a valid area! srcPos: $srcPos endPos: $endPos" }

        this.srcPos = Vector2i(srcPos)
        this.endPos = Vector2i(endPos)
    }

    companion object {
        private fun validArea(srcPos: Vector2i, endPos: Vector2i): Boolean {
            return srcPos.x <= endPos.x && srcPos.y <= endPos.y
        }
    }
}