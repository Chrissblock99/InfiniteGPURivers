package me.chriss99.erosion

import me.chriss99.Area
import me.chriss99.worldmanagement.iteration.IterableWorld
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i

class ErosionManager(private val eroder: GPUTerrainEroder, private val data: IterableWorld) {
    private val maxChunks: Vec2i = eroder.maxTextureSize / data.chunkSize
    private var currentTask: ErosionTask? = null

    fun findIterate(pos: Vec2i, maxIteration: Int, iterations: Int): Boolean {
        if (currentTask == null) {
            currentTask = findChangeArea(pos, maxIteration, iterations)
            if (currentTask == null)
                return false
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
    }

    private fun findChangeArea(pos: Vec2i, maxIteration: Int, maxSurface: Int): ErosionTask? {
        val findArea = Area(eroder.maxTextureSize / data.chunkSize) + pos - (eroder.maxTextureSize / data.chunkSize / 2)
        val intersection = findArea intersect (eroder.usedArea / data.chunkSize)

        if (intersection != null) {
            val task: ErosionTask? = findTask(intersection, maxIteration, maxSurface)
            if (task != null) return task
        }


        val task: ErosionTask = findTask(findArea, maxIteration, maxSurface) ?: return null

        eroder.usedArea = findArea * data.chunkSize
        return task
    }

    private fun findTask(area: Area, maxIteration: Int, maxSurface: Int): ErosionTask? {
        val lowestIterable = lowestIterableTiles(area, maxIteration) ?: return null

        return bruteForceTask(area, lowestIterable, maxSurface)
    }

    private fun bruteForceTask(allowed: Area, searchInside: LinkedHashSet<Vec2i>, maxSurface: Int): ErosionTask? {
        var bestArea = Area()

        for (currentPos in searchInside) {
            val betterArea = betterAreaFrom(currentPos, bestArea, allowed, maxSurface)
            if (betterArea != null) bestArea = betterArea

            if (bestArea.size == maxChunks) break
        }

        if (bestArea == Area()) return null

        return createTask(bestArea)
    }

    private fun lowestIterableTiles(area: Area, maxIteration: Int): LinkedHashSet<Vec2i>? {
        val tilesAtIteration: HashMap<Int, LinkedHashSet<Vec2i>> = LinkedHashMap()

        area.innerPoints.forEach {
            val iteration = data[it].iteration
            tilesAtIteration.computeIfAbsent(iteration) { _ -> LinkedHashSet() }.add(it)
        }

        val sortedIterations = tilesAtIteration.keys.sorted()

        var lowestIterable: LinkedHashSet<Vec2i>? = null
        for (lowestIteration in sortedIterations) {
            if (lowestIteration > maxIteration) return null

            val currentCandidates = tilesAtIteration[lowestIteration]!!
            if (hasIterable2x2Area(currentCandidates)) {
                lowestIterable = currentCandidates
                break
            }
        }

        return lowestIterable
    }

    private fun hasIterable2x2Area(tiles: LinkedHashSet<Vec2i>) = tiles.any {
        (it + Vec2i(1, 0)) in tiles &&
        (it + Vec2i(0, 1)) in tiles &&
        (it + Vec2i(1, 1)) in tiles &&
        iterable(it)
    }

    private fun betterAreaFrom(startPos: Vec2i, bestArea: Area, allowedArea: Area, maxSurface: Int): Area? {
        var betterArea = Area(startPos, 2)

        if (betterArea !in allowedArea || !iterable(betterArea)) return null

        val directions = booleanArrayOf(true, true, true, true)

        var i = 0
        while (directions.any { it }) {
            if (betterArea.width == maxChunks.x) {
                directions[0] = false
                directions[2] = false
            }
            if (betterArea.height == maxChunks.y) {
                directions[1] = false
                directions[3] = false
            }


            if (!directions[i]) {
                i = (i + 1) % 4
                continue
            }

            val betterAreaTry = betterArea.increase(changes[i].x, changes[i].y, changes[i].z, changes[i].w)

            if (betterAreaTry in allowedArea && (betterAreaTry * data.chunkSize).area < maxSurface && iterable(betterAreaTry))
                betterArea = betterAreaTry
            else directions[i] = false
            i = (i + 1) % 4
        }


        if (betterArea.area > bestArea.area) return betterArea
        return null
    }

    private fun iterable(area: Area): Boolean {
        return area.increase(-1, -1, 0, 0) .innerPoints.all { iterable(it) }
    }

    private fun iterable(pos: Vec2i): Boolean {
        val l = data[pos + Vec2i(0, 1)].horizontal
        val r = data[pos + Vec2i(1, 1)].horizontal
        val f = data[pos + Vec2i(1, 1)].vertical
        val b = data[pos + Vec2i(1, 0)].vertical

        if (l == -1 || r == 1 || f == 1 || b == -1) return false

        val tl = data[pos + Vec2i(0, 1)].vertical == 0
        val tr = data[pos + Vec2i(2, 1)].vertical == 0
        val dl = data[pos + Vec2i(0, 0)].vertical == 0
        val dr = data[pos + Vec2i(2, 0)].vertical == 0

        return !(l == 0 && f == 0 && !tl || f == 0 && r == 0 && !tr || l == 0 && b == 0 && !dl || b == 0 && r == 0 && !dr)
    }

    private fun createTask(area: Area): ErosionTask {
        val length = area.size - 1

        val l = getEdgesEqual(area.srcPos, length.y, true)
        val r = getEdgesEqual(area.srcPos.plus(length.x, 0), length.y, true)
        val f = getEdgesEqual(area.srcPos.plus(0, length.y), length.x, false)
        val b = getEdgesEqual(area.srcPos, length.x, false)

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

    /**
     *
     * @param pos where to start checking for equality
     * @param length how far to check for equality
     * @param upwards whether to check for equality upwards or sideways
     * @return -1, 0 or 1 if all are equal, else the length at which it isn't equal (larger than 1)
     */
    private fun getEdgesEqual(pos: Vec2i, length: Int, upwards: Boolean): Int {
        val edge = getEdge(pos, 1, upwards)

        for (i in 2..length) if (getEdge(pos, i, upwards) != edge) return i

        return edge
    }

    private fun getEdge(pos: Vec2i, offset: Int, upwards: Boolean): Int {
        return if (upwards)
            data[Vec2i(pos).plus(0, offset)].horizontal
        else
            data[Vec2i(pos).plus(offset, 0)].vertical
    }

    private fun setEdges(pos: Vec2i, length: Int, upwards: Boolean, value: Int) {
        for (i in 1..length)
            if (upwards)
                data[Vec2i(pos).plus(0, i)].horizontal = value
            else
                data[Vec2i(pos).plus(i, 0)].vertical = value
    }

    companion object {
        private val changes =
            arrayOf(Vec4i(0, 0, 1, 0), Vec4i(0, 0, 0, 1), Vec4i(1, 0, 0, 0), Vec4i(0, 1, 0, 0))
    }
}