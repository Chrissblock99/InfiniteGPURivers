package me.chriss99.erosion

import me.chriss99.Area
import me.chriss99.IterationSurfaceType
import me.chriss99.worldmanagement.iteration.IterableWorld
import org.joml.Vector2i
import org.joml.Vector4i

class ErosionManager(eroder: GPUTerrainEroder, private val data: IterableWorld) {
    private val eroder: GPUTerrainEroder = eroder

    private val maxChunks: Vector2i = Vector2i(eroder.getMaxTextureSize()).div(data.chunkSize)

    private var currentTask: ErosionTask? = null

    fun findIterate(pos: Vector2i, maxIteration: Int, iterations: Int): Boolean {
        if (currentTask == null) {
            currentTask = findChangeArea(pos, maxIteration, iterations)
            if (currentTask == null)
                return false
        }

        var done = false
        var i = 0
        while (!done) {
            val nextIterations: Int = currentTask!!.nextIterations()
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

    private fun findChangeArea(pos: Vector2i, maxIteration: Int, maxSurface: Int): ErosionTask? {
        val findArea: Area = Area(eroder.getMaxTextureSize().div(data.chunkSize)).add(pos)
            .sub(eroder.getMaxTextureSize().div(data.chunkSize).div(2))
        val intersection = findArea.intersection(eroder.getUsedArea().div(data.chunkSize))

        if (intersection != null) {
            val task: ErosionTask? = findTask(intersection, maxIteration, maxSurface)
            if (task != null) return task
        }


        val task: ErosionTask = findTask(findArea, maxIteration, maxSurface) ?: return null

        eroder.changeArea(findArea.mul(data.chunkSize))
        return task
    }

    private fun findTask(area: Area, maxIteration: Int, maxSurface: Int): ErosionTask? {
        val lowestIterable = lowestIterableTiles(area, maxIteration) ?: return null

        return bruteForceTask(area, lowestIterable, maxSurface)
    }

    private fun bruteForceTask(allowed: Area, searchInside: LinkedHashSet<Vector2i>, maxSurface: Int): ErosionTask? {
        var bestArea = Area()

        for (currentPos in searchInside) {
            val betterArea = betterAreaFrom(currentPos, bestArea, allowed, maxSurface)
            if (betterArea != null) bestArea = betterArea

            if (bestArea.size == maxChunks) break
        }

        if (bestArea == Area()) return null

        return createTask(bestArea)
    }

    private fun lowestIterableTiles(area: Area, maxIteration: Int): LinkedHashSet<Vector2i>? {
        val tilesAtIteration: HashMap<Int, LinkedHashSet<Vector2i>> = LinkedHashMap()

        area.forAllPoints { pos: Vector2i ->
            val iteration = data.getTile(pos).iteration
            tilesAtIteration.computeIfAbsent(iteration) { k: Int? -> LinkedHashSet() }.add(pos!!)
        }

        val sortedIterations = tilesAtIteration.keys.stream().sorted(Comparator.naturalOrder()).toList()

        var lowestIterable: LinkedHashSet<Vector2i>? = null
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

    private fun hasIterable2x2Area(tiles: LinkedHashSet<Vector2i>): Boolean {
        for (pos in tiles) if (tiles.contains(Vector2i(pos).add(1, 0)) && tiles.contains(
                Vector2i(pos).add(
                    0,
                    1
                )
            ) && tiles.contains(Vector2i(pos).add(1, 1)) && iterable(
                Area(pos, 2)
            )
        ) return true

        return false
    }

    private fun betterAreaFrom(startPos: Vector2i, bestArea: Area, allowedArea: Area, maxSurface: Int): Area? {
        var betterArea = Area(startPos, 2)

        if (!iterable(betterArea)) return null

        val directions = booleanArrayOf(true, true, true, true)

        var i = 0
        while (directions[0] || directions[1] || directions[2] || directions[3]) {
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

            if (allowedArea.contains(betterAreaTry) && betterAreaTry.mul(data.chunkSize).area < maxSurface && iterable(
                    betterAreaTry
                )
            ) betterArea = betterAreaTry
            else directions[i] = false
            i = (i + 1) % 4
        }


        if (betterArea.area > bestArea.area) return betterArea
        return null
    }

    private fun iterable(area: Area): Boolean {
        if (area.width < 2 || area.height < 2) return false

        val length = area.size.sub(1, 1)

        val l = getEdgesEqual(area.srcPos(), length.y, true)
        val r = getEdgesEqual(area.srcPos().add(length.x, 0), length.y, true)
        val f = getEdgesEqual(area.srcPos().add(0, length.y), length.x, false)
        val b = getEdgesEqual(area.srcPos(), length.x, false)

        if (l > 1 || r > 1 || f > 1 || b > 1) return false

        if (l == -1 || r == 1 || f == 1 || b == -1) return false

        val allowed = IterationSurfaceType.SurfaceType.FLAT
        val tl = allowed == data.getIterationSurfaceType(area.srcPos().add(0, length.y)).surfaceType
        val tr = allowed == data.getIterationSurfaceType(area.srcPos().add(length)).surfaceType
        val dl = allowed == data.getIterationSurfaceType(area.srcPos()).surfaceType
        val dr = allowed == data.getIterationSurfaceType(area.srcPos().add(length.x, 0)).surfaceType

        if (l == 0 && f == 0 && !tl || f == 0 && r == 0 && !tr || l == 0 && b == 0 && !dl || b == 0 && r == 0 && !dr) return false

        return iterationsAreSame(area)
    }

    private fun createTask(area: Area): ErosionTask {
        val length = area.size.sub(1, 1)

        val l = getEdgesEqual(area.srcPos(), length.y, true)
        val r = getEdgesEqual(area.srcPos().add(length.x, 0), length.y, true)
        val f = getEdgesEqual(area.srcPos().add(0, length.y), length.x, false)
        val b = getEdgesEqual(area.srcPos(), length.x, false)

        return ErosionTask(eroder, area.mul(data.chunkSize), data.chunkSize, l, r, f, b)
    }

    private fun taskFinished(task: ErosionTask) {
        val area: Area = task.getArea().div(data.chunkSize)
        val length = area.size.sub(1, 1)

        val l: Int = task.l - 1
        val r: Int = task.r + 1
        val f: Int = task.f + 1
        val b: Int = task.b - 1

        setEdges(area.srcPos(), length.y, true, l)
        setEdges(area.srcPos().add(length.x, 0), length.y, true, r)
        setEdges(area.srcPos().add(0, length.y), length.x, false, f)
        setEdges(area.srcPos(), length.x, false, b)

        increaseIteration(area, l, r, f, b)
    }

    private fun increaseIteration(area: Area, l: Int, r: Int, f: Int, b: Int) {
        val inner = area.outset(-1)
        val size = area.size.sub(1, 1)

        inner.forAllPoints { v: Vector2i -> data.getTile(v)!!.iteration += data.chunkSize }

        if (l == 0) for (y in 1..<size.y) data.getTile(area.srcPos().add(0, y))!!.iteration += data.chunkSize
        if (r == 0) for (y in 1..<size.y) data.getTile(area.srcPos().add(size.x, y))!!.iteration += data.chunkSize
        if (f == 0) for (x in 1..<size.x) data.getTile(area.srcPos().add(x, size.y))!!.iteration += data.chunkSize
        if (b == 0) for (x in 1..<size.x) data.getTile(area.srcPos().add(x, 0))!!.iteration += data.chunkSize

        if (l == 0 && b == 0) data.getTile(area.srcPos())!!.iteration += data.chunkSize
        if (l == 0 && f == 0) data.getTile(area.srcPos().add(0, size.y))!!.iteration += data.chunkSize
        if (r == 0 && b == 0) data.getTile(area.srcPos().add(size.x, 0))!!.iteration += data.chunkSize
        if (r == 0 && f == 0) data.getTile(inner.endPos())!!.iteration += data.chunkSize
    }

    private fun iterationsAreSame(area: Area): Boolean {
        val iteration = data.getTile(area.srcPos())!!.iteration

        for (pos in area.allPoints()) if (iteration != data.getTile(pos)!!.iteration) return false
        return true
    }

    /**
     *
     * @param pos where to start checking for equality
     * @param length how far to check for equality
     * @param upwards whether to check for equality upwards or sideways
     * @return -1, 0 or 1 if all are equal, else the length at which it isn't equal (larger than 1)
     */
    private fun getEdgesEqual(pos: Vector2i, length: Int, upwards: Boolean): Int {
        val edge = getEdge(pos, 1, upwards)

        for (i in 2..length) if (getEdge(pos, i, upwards) != edge) return i

        return edge
    }

    private fun getEdge(pos: Vector2i, offset: Int, upwards: Boolean): Int {
        return if (upwards) data.getTile(Vector2i(pos).add(0, offset))!!.horizontal else data.getTile(
            Vector2i(pos).add(
                offset,
                0
            )
        )!!.vertical
    }

    private fun setEdges(pos: Vector2i, length: Int, upwards: Boolean, value: Int) {
        for (i in 1..length) if (upwards) data.getTile(Vector2i(pos).add(0, i))!!.horizontal = value
        else data.getTile(Vector2i(pos).add(i, 0))!!.vertical = value
    }

    companion object {
        private val changes =
            arrayOf(Vector4i(0, 0, 1, 0), Vector4i(0, 0, 0, 1), Vector4i(1, 0, 0, 0), Vector4i(0, 1, 0, 0))
    }
}