package me.chriss99.erosion

import me.chriss99.Area
import glm_.vec2.Vec2i
import me.chriss99.worldmanagement.ErosionDataStorage
import kotlin.math.max
import kotlin.math.min

class ErosionManager(pos: Vec2i, maxTextureSize: Vec2i, worldStorage: ErosionDataStorage, targetIteration: Int) {
    private val data = worldStorage.iterationInfo
    private val maxChunks: Vec2i = maxTextureSize / data.chunkSize

    var targetIteration = (targetIteration/data.chunkSize + 1) * data.chunkSize
        set(value) {
            field = (value/data.chunkSize + 1) * data.chunkSize
        }

    private var currentArea: Area = findNewArea(pos) ?: Area(maxChunks)
    private var iterabilityInfo: Array<Array<IterabilityInfo?>> = computeIterability(currentArea.srcPos)
    private var currentTask: ErosionTask? = null

    private val eroder: GPUTerrainEroder = GPUTerrainEroder(worldStorage, maxTextureSize, currentArea * data.chunkSize)
    val usedArea: Area get() = eroder.usedArea

    fun downloadMap() = eroder.downloadMap()
    val iterabilityInfoCopy get() = iterabilityInfo.copyOf()

    fun findIterate(pos: Vec2i, iterations: Int): Boolean {
        if (currentTask == null) {
            currentTask = findTask(iterations)
            if (currentTask == null) {
                if (!findAndUseNewArea(pos))
                    return false
                currentTask = findTask(iterations)
                if (currentTask == null)
                    return false
            }
        }

        var done = false
        var i = 0
        while (!done) {
            val nextIterations = currentTask!!.nextIterations
            if (i != 0 && i + nextIterations > iterations) break

            done = currentTask!!.erosionStep()
            i += nextIterations
        }

        if (done) {
            taskFinished(currentTask!!)
            currentTask = null
        }
        return true
    }

    fun finishRunningTasks() {
        if (currentTask == null) return

        while (!currentTask!!.erosionStep());
        taskFinished(currentTask!!)
        currentTask = null
        eroder.downloadMap()
    }

    private fun findNewArea(pos: Vec2i): Area? {
        val area = Area(maxChunks) + pos - (maxChunks / 2)
        return if (anyIterable(computeIterability(area.srcPos))) area else null
    }

    private fun findAndUseNewArea(pos: Vec2i): Boolean {
        currentArea = findNewArea(pos) ?: return false
        iterabilityInfo = computeIterability(currentArea.srcPos)
        eroder.usedArea = currentArea * data.chunkSize
        return true
    }

    private fun computeIterability(pos: Vec2i): Array<Array<IterabilityInfo?>> {
        return Array(maxChunks.y - 1) { y ->
            Array(maxChunks.x - 1) { x ->
                iterabilityInfo(pos + Vec2i(x, y))
            }
        }
    }

    private fun anyIterable(iterabilityInfo: Array<Array<IterabilityInfo?>>): Boolean {
        return iterabilityInfo.any { arr -> arr.any { (it?.iteration ?: Int.MAX_VALUE) < targetIteration } }
    }

    private fun findTask(maxSurface: Int): ErosionTask? {
        val xSize = iterabilityInfo[0].size

        val height = Array(xSize) { 0 }
        val left = Array(xSize) { 0 }
        val right = Array(xSize) { xSize }

        var maxArea = 0
        var minIteration = targetIteration-1
        var maxNonZeroEdges = 0

        var area = Area()
        var l = 0
        var r = 0
        var f = 0
        var b = 0

        for (y in 0..<iterabilityInfo.size) {
            var curLeft = 0
            var curRight = xSize

            for (x in 0..<xSize)
                if (iterabilityInfo[y][x] != null) {
                    height[x]++
                    left[x] = max(left[x], curLeft)
                } else {
                    height[x] = 0
                    left[x] = 0
                    curLeft = x + 1
                }

            for (x in xSize-1 downTo 0)
                if (iterabilityInfo[y][x] != null) {
                    right[x] = min(right[x], curRight)

                    val currentArea = height[x] * (right[x] - left[x])
                    val currentL = iterabilityInfo[y][left[x]]!!.l
                    val currentR = iterabilityInfo[y][right[x]-1]!!.r
                    val currentF = iterabilityInfo[y][x]!!.f
                    val currentB = iterabilityInfo[y-height[x]+1][x]!!.b
                    val currentNonZeroEdges = (if (currentL == 0) 0 else 1) + (if (currentR == 0) 0 else 1) +
                            (if (currentF == 0) 0 else 1) + (if (currentB == 0) 0 else 1)
                    val currentIteration = iterabilityInfo[y][x]!!.iteration

                    if (isBetterArea(
                            maxArea,
                            currentArea,
                            minIteration,
                            currentIteration,
                            maxNonZeroEdges,
                            currentNonZeroEdges
                        )
                    ) {
                        maxArea = currentArea
                        minIteration = currentIteration
                        maxNonZeroEdges = currentNonZeroEdges

                        area = Area(Vec2i(right[x] - left[x], height[x])) + Vec2i(left[x], y-height[x]+1)
                        l = currentL
                        r = currentR
                        f = currentF
                        b = currentB
                    }
                } else {
                    right[x] = xSize
                    curRight = x
                }
        }

        if (maxArea == 0)
            return null

        return createTask(area.increase(1, 1, 0, 0) + currentArea.srcPos, l, r, f, b)
    }

    private fun isBetterArea(maxArea: Int, currentArea: Int,
                             minIteration: Int, currentIteration: Int,
                             maxNonZeroEdges: Int, currentNonZeroEdges: Int): Boolean {
        if (currentIteration != minIteration)
            return currentIteration < minIteration
        if (currentNonZeroEdges != maxNonZeroEdges)
            return currentNonZeroEdges > maxNonZeroEdges
        if (currentArea != maxArea)
            return currentArea > maxArea
        return false
    }

    private fun iterabilityInfo(pos: Vec2i): IterabilityInfo? {
        val l = data[pos + Vec2i(0, 1)].horizontal
        val r = data[pos + Vec2i(1, 1)].horizontal
        val f = data[pos + Vec2i(1, 1)].vertical
        val b = data[pos + Vec2i(1, 0)].vertical

        if (l == -1 || r == 1 || f == 1 || b == -1) return null

        val tl = data[pos + Vec2i(0, 1)].vertical == 0
        val tr = data[pos + Vec2i(2, 1)].vertical == 0
        val dl = data[pos + Vec2i(0, 0)].vertical == 0
        val dr = data[pos + Vec2i(2, 0)].vertical == 0

        if (l == 0 && f == 0 && !tl || f == 0 && r == 0 && !tr || l == 0 && b == 0 && !dl || b == 0 && r == 0 && !dr)
            return null

        return IterabilityInfo(l, r, f, b, data[pos].iteration)
    }

    data class IterabilityInfo(val l: Int, val r: Int, val f: Int, val b: Int, val iteration: Int)

    private fun createTask(area: Area, l: Int, r: Int, f: Int, b: Int): ErosionTask {
        return ErosionTask(eroder, area * data.chunkSize, data.chunkSize, l, r, f, b)
    }

    private fun taskFinished(task: ErosionTask) {
        val area = task.area / data.chunkSize
        val length = area.size - 1

        val l: Int = task.l - 1
        val r: Int = task.r + 1
        val f: Int = task.f + 1
        val b: Int = task.b - 1

        setEdges(area.srcPos, length.y, true, l)
        setEdges(area.srcPos.plus(length.x, 0), length.y, true, r)
        setEdges(area.srcPos.plus(0, length.y), length.x, false, f)
        setEdges(area.srcPos, length.x, false, b)

        increaseIteration(area, l, r, f, b)
        iterabilityInfo = computeIterability(currentArea.srcPos)
    }

    private fun increaseIteration(area: Area, l: Int, r: Int, f: Int, b: Int) {
        val inner = area.outset(-1)
        val size = area.size - 1

        inner.innerPoints.forEach { data[it].iteration += data.chunkSize }

        if (l == 0) for (y in 1..<size.y) data[area.srcPos.plus(0, y)].iteration += data.chunkSize
        if (r == 0) for (y in 1..<size.y) data[area.srcPos.plus(size.x, y)].iteration += data.chunkSize
        if (f == 0) for (x in 1..<size.x) data[area.srcPos.plus(x, size.y)].iteration += data.chunkSize
        if (b == 0) for (x in 1..<size.x) data[area.srcPos.plus(x, 0)].iteration += data.chunkSize

        if (l == 0 && b == 0) data[area.srcPos].iteration += data.chunkSize
        if (l == 0 && f == 0) data[area.srcPos.plus(0, size.y)].iteration += data.chunkSize
        if (r == 0 && b == 0) data[area.srcPos.plus(size.x, 0)].iteration += data.chunkSize
        if (r == 0 && f == 0) data[inner.endPos].iteration += data.chunkSize
    }

    private fun setEdges(pos: Vec2i, length: Int, upwards: Boolean, value: Int) {
        for (i in 1..length)
            if (upwards)
                data[Vec2i(pos).plus(0, i)].horizontal = value
            else
                data[Vec2i(pos).plus(i, 0)].vertical = value
    }

    fun delete() {
        eroder.delete()
    }
}