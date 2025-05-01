package me.chriss99

import me.chriss99.util.Util
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec3.Vec3d
import java.util.*

object ColoredVAOGenerator {
    fun areaToSimpleVertexes(area: Area, height: Double): DoubleArray {
        val vertecies = DoubleArray(4 * 3)
        var vertexShift = 0

        vertecies[vertexShift] = area.srcPos().x.toDouble()
        vertecies[vertexShift + 1] = height
        vertecies[vertexShift + 2] = area.srcPos().y.toDouble()
        vertexShift += 3

        vertecies[vertexShift] = area.endPos().x.toDouble()
        vertecies[vertexShift + 1] = height
        vertecies[vertexShift + 2] = area.srcPos().y.toDouble()
        vertexShift += 3

        vertecies[vertexShift] = area.srcPos().x.toDouble()
        vertecies[vertexShift + 1] = height
        vertecies[vertexShift + 2] = area.endPos().y.toDouble()
        vertexShift += 3

        vertecies[vertexShift] = area.endPos().x.toDouble()
        vertecies[vertexShift + 1] = height
        vertecies[vertexShift + 2] = area.endPos().y.toDouble()

        return vertecies
    }

    fun areaToSimpleColors(red: Double, green: Double, blue: Double): DoubleArray {
        val color = DoubleArray(4 * 3)
        var vertexShift = 0

        for (i in 0..3) {
            color[vertexShift] = red
            color[vertexShift + 1] = green
            color[vertexShift + 2] = blue
            vertexShift += 3
        }

        return color
    }

    fun areaToColoredVAO(area: Area, height: Double, red: Double, green: Double, blue: Double): ColoredVAO {
        val vertexes = areaToSimpleVertexes(area, height)
        val color = areaToSimpleColors(red, green, blue)
        val index = heightMapToSimpleIndex(Vec2i(2))

        return ColoredVAO(vertexes, color, index)
    }

    fun heightMapToSimpleVertexes(heightMap: Array<DoubleArray>, water: Boolean): DoubleArray {
        val vertecies = DoubleArray(heightMap.size * heightMap[0].size * 3)
        var vertexShift = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            vertecies[vertexShift] = x.toDouble()
            vertecies[vertexShift + 1] = heightMap[x][z] - (if (water) .03 else 0.0)
            vertecies[vertexShift + 2] = z.toDouble()

            vertexShift += 3
        }

        return vertecies
    }

    fun heightMapToSimpleColors(heightMap: Array<DoubleArray>, min: Double, max: Double, water: Boolean): DoubleArray {
        val color = DoubleArray(heightMap.size * heightMap[0].size * 3)
        var vertexShift = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            val gradient = (heightMap[x][z] - min) / (max - min)

            if (!water) {
                color[vertexShift] = gradient * .7 + .3
                color[vertexShift + 1] = gradient * .8 + .2
                color[vertexShift + 2] = if (x % 2 == 0) .33 else 0 + (if (z % 2 == 0) .33 else 0.0)
            } else {
                color[vertexShift] = 0.0
                color[vertexShift + 1] = if (x % 2 == 0) .33 else 0 + (if (z % 2 == 0) .33 else 0.0)
                color[vertexShift + 2] = gradient
            }

            vertexShift += 3
        }

        return color
    }

    fun heightMapToSimpleIndex(size: Vec2i): IntArray {
        val index = IntArray((size.x - 1) * (size.y - 1) * 6)
        var indexShift = 0

        for (z in 0..<size.y) for (x in 0..<size.x) {
            if (z == size.y - 1 || x == size.x - 1) continue

            index[indexShift + 0] = Util.indexOfXZFlattenedArray(x, z, size.x)
            index[indexShift + 1] = Util.indexOfXZFlattenedArray(x + 1, z, size.x)
            index[indexShift + 2] = Util.indexOfXZFlattenedArray(x, z + 1, size.x)
            index[indexShift + 3] = Util.indexOfXZFlattenedArray(x + 1, z, size.x)
            index[indexShift + 4] = Util.indexOfXZFlattenedArray(x + 1, z + 1, size.x)
            index[indexShift + 5] = Util.indexOfXZFlattenedArray(x, z + 1, size.x)
            indexShift += 6
        }

        return index
    }

    fun heightMapToSimpleVAO(heightMap: Array<DoubleArray>, min: Double, max: Double, water: Boolean): ColoredVAO {
        val vertexes = heightMapToSimpleVertexes(heightMap, water)
        val color = heightMapToSimpleColors(heightMap, min, max, water)
        val index = heightMapToSimpleIndex(Vec2i(heightMap.size, heightMap[0].size))

        return ColoredVAO(vertexes, color, index)
    }

    fun heightMapToSquareVertexes(heightMap: Array<DoubleArray>): DoubleArray {
        val vertexes = DoubleArray(heightMap.size * heightMap[0].size * 4 * 3)
        var vertexShift = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) for (n in 0..3) {
            vertexes[vertexShift] = x + (if (n > 1) 1 else 0) - .5
            vertexes[vertexShift + 1] = heightMap[x][z]
            vertexes[vertexShift + 2] = z + (if (n % 2 == 0) 1 else 0) - .5
            vertexShift += 3
        }

        return vertexes
    }

    fun heightMapToSquareColors(heightMap: Array<DoubleArray>): DoubleArray {
        val color = DoubleArray(heightMap.size * heightMap[0].size * 4 * 3)
        for (i in color.indices) color[i] = Math.random() * .5 + .5

        return color
    }

    fun heightMapToSquareIndex(heightMap: Array<DoubleArray>): IntArray {
        val index = IntArray(heightMap.size * heightMap[0].size * 6)
        var indexShift = 0
        var indexShift2 = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            index[indexShift + 0] = indexShift2 + 0
            index[indexShift + 1] = indexShift2 + 1
            index[indexShift + 2] = indexShift2 + 2
            index[indexShift + 3] = indexShift2 + 1
            index[indexShift + 4] = indexShift2 + 3
            index[indexShift + 5] = indexShift2 + 2
            indexShift += 6
            indexShift2 += 4
        }

        return index
    }

    fun heightMapToSquareVAO(heightMap: Array<DoubleArray>): ColoredVAO {
        val vertexes = heightMapToSquareVertexes(heightMap)
        val color = heightMapToSquareColors(heightMap)
        val index = heightMapToSquareIndex(heightMap)

        return ColoredVAO(vertexes, color, index)
    }

    private val offsets = arrayOf(
        doubleArrayOf(.5, .5),
        doubleArrayOf(-.5, .5),
        doubleArrayOf(-.5, .5),
        doubleArrayOf(-.5, -.5),
        doubleArrayOf(.5, -.5),
        doubleArrayOf(.5, .5),
        doubleArrayOf(-.5, -.5),
        doubleArrayOf(.5, -.5)
    )

    fun heightMapToCrossVertexes(heightMap: Array<DoubleArray>): DoubleArray {
        val vertexes = DoubleArray(heightMap.size * heightMap[0].size * 9 * 3)
        var vertexShift = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            for (i in 0..7) {
                vertexes[vertexShift] = x + offsets[i][0]
                vertexes[vertexShift + 1] = heightMap[x][z]
                vertexes[vertexShift + 2] = z + offsets[i][1]
                vertexShift += 3
            }

            vertexes[vertexShift] = x.toDouble()
            vertexes[vertexShift + 1] = heightMap[x][z]
            vertexes[vertexShift + 2] = z.toDouble()
            vertexShift += 3
        }

        return vertexes
    }

    fun heightMapToCrossColors(heightMap: Array<DoubleArray>, outflowPipes: Array<Array<DoubleArray>>): DoubleArray {
        val color = DoubleArray(heightMap.size * heightMap[0].size * 9 * 3)
        var vertexShift = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            for (i in 0..7) {
                color[vertexShift] = -outflowPipes[x][z][i / 2] * 100
                color[vertexShift + 1] = outflowPipes[x][z][i / 2] * 100
                color[vertexShift + 2] = 0.0
                vertexShift += 3
            }

            color[vertexShift] = 0.0
            color[vertexShift + 1] = 0.0
            color[vertexShift + 2] = .5
            vertexShift += 3
        }

        return color
    }

    fun heightMapToCrossIndex(heightMap: Array<DoubleArray>): IntArray {
        val index = IntArray(heightMap.size * heightMap[0].size * 12)
        var indexShift = 0
        var indexShift2 = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            var indexShift3 = 0
            for (i in 0..3) {
                index[indexShift] = indexShift2 + indexShift3
                index[indexShift + 1] = indexShift2 + indexShift3 + 1
                index[indexShift + 2] = indexShift2 + 8
                indexShift3 += 2
                indexShift += 3
            }
            indexShift2 += 9
        }

        return index
    }

    fun heightMapToCrossVAO(heightMap: Array<DoubleArray>, outflowPipes: Array<Array<DoubleArray>>): ColoredVAO {
        val vertexes = heightMapToCrossVertexes(heightMap)
        val color = heightMapToCrossColors(heightMap, outflowPipes)
        val index = heightMapToCrossIndex(heightMap)

        return ColoredVAO(vertexes, color, index)
    }

    fun heightMapToVecVertexes(heightMap: Array<DoubleArray>, VecField: Array<Array<DoubleArray>>): DoubleArray {
        val vertexes = DoubleArray(heightMap.size * heightMap[0].size * 3 * 3)
        var vertexShift = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            val vec: Vec2d = Vec2d(VecField[x][z][0], VecField[x][z][1])
            vec.normalize().times(.3)

            vertexes[vertexShift] = x - vec.y * .6 - vec.x
            vertexes[vertexShift + 1] = heightMap[x][z] + .1
            vertexes[vertexShift + 2] = z + vec.x * .6 - vec.y
            vertexShift += 3

            vertexes[vertexShift] = x + vec.y * .6 - vec.x
            vertexes[vertexShift + 1] = heightMap[x][z] + .1
            vertexes[vertexShift + 2] = z - vec.x * .6 - vec.y
            vertexShift += 3

            vertexes[vertexShift] = x + vec.x * 1.5
            vertexes[vertexShift + 1] = heightMap[x][z] + .1
            vertexes[vertexShift + 2] = z + vec.y * 1.5
            vertexShift += 3
        }

        return vertexes
    }

    fun heightMapToVecColors(heightMap: Array<DoubleArray>): DoubleArray {
        val color = DoubleArray(heightMap.size * heightMap[0].size * 3 * 3)
        Arrays.fill(color, 1.0)

        return color
    }

    fun heightMapToVecIndex(heightMap: Array<DoubleArray>): IntArray {
        val index = IntArray(heightMap.size * heightMap[0].size * 3)
        var indexShift = 0
        var indexShift2 = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            index[indexShift] = indexShift2
            index[indexShift + 1] = indexShift2 + 1
            index[indexShift + 2] = indexShift2 + 2
            indexShift += 3
            indexShift2 += 3
        }

        return index
    }

    fun heightMapToVecVAO(heightMap: Array<DoubleArray>, VecField: Array<Array<DoubleArray>>): ColoredVAO {
        val vertexes = heightMapToVecVertexes(heightMap, VecField)
        val color = heightMapToVecColors(heightMap)
        val index = heightMapToVecIndex(heightMap)

        return ColoredVAO(vertexes, color, index)
    }

    fun heightMapToNormalVertexes(heightMap: Array<DoubleArray>): DoubleArray {
        val vertexes = DoubleArray(heightMap.size * heightMap[0].size * 3 * 3)
        var vertexShift = 0

        for (z in heightMap[0].indices) for (x in heightMap.indices) {
            val normal: Vec3d = normalAt(heightMap, x, z)

            vertexes[vertexShift] = x - normal.z * .3
            vertexes[vertexShift + 1] = heightMap[x][z] + .1
            vertexes[vertexShift + 2] = z + normal.x * .3
            vertexShift += 3

            vertexes[vertexShift] = x + normal.z * .3
            vertexes[vertexShift + 1] = heightMap[x][z] + .1
            vertexes[vertexShift + 2] = z - normal.x * .3
            vertexShift += 3

            vertexes[vertexShift] = x + normal.x
            vertexes[vertexShift + 1] = heightMap[x][z] + normal.y + .1
            vertexes[vertexShift + 2] = z + normal.z
            vertexShift += 3
        }

        return vertexes
    }

    fun heightMapToNormalVAO(heightMap: Array<DoubleArray>): ColoredVAO {
        val vertexes = heightMapToNormalVertexes(heightMap)
        val color = heightMapToVecColors(heightMap)
        val index = heightMapToVecIndex(heightMap)

        return ColoredVAO(vertexes, color, index)
    }

    fun tesselationGridVertexesTest(xSize: Int, zSize: Int, step: Double): DoubleArray {
        val vertexes = DoubleArray(xSize * zSize * 8)
        var i = 0

        for (z in 0..<zSize) for (x in 0..<xSize) {
            vertexes[i] = x * step
            vertexes[i + 1] = z * step

            vertexes[i + 2] = (x + 1) * step
            vertexes[i + 3] = z * step

            vertexes[i + 4] = x * step
            vertexes[i + 5] = (z + 1) * step

            vertexes[i + 6] = (x + 1) * step
            vertexes[i + 7] = (z + 1) * step

            i += 8
        }

        return vertexes
    }

    fun normalAt(heightMap: Array<DoubleArray>, x: Int, z: Int): Vec3d {
        val heights = DoubleArray(4)
        for (i in vonNeumannNeighbourhood.indices) heights[i] =
            heightMap[wrapOffsetCoordinateVonNeumann(x, heightMap.size, i, 0)][wrapOffsetCoordinateVonNeumann(
                z,
                heightMap[0].size,
                i,
                1
            )]

        return Vec3d(heights[1] - heights[2], 1.0, heights[3] - heights[0]).normalize()
    }

    fun wrapOffsetCoordinateVonNeumann(index: Int, length: Int, offset: Int, xz: Int): Int {
        return wrapNumber(index + vonNeumannNeighbourhood[offset][xz], length)
    }

    //  0
    //1   2
    //  3
    val vonNeumannNeighbourhood: Array<IntArray> = arrayOf(
        intArrayOf(0, 1),
        intArrayOf(-1, 0),
        intArrayOf(1, 0),
        intArrayOf(0, -1)
    )

    fun wrapNumber(num: Int, length: Int): Int {
        return (num + length) % length
    }
}