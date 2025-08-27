package me.chriss99.render

import me.chriss99.glabstractions.VAOImpl

open class ColoredVAO(triangle: DoubleArray, color: DoubleArray, index: IntArray) :
    VAOImpl(index, 3, triangle, color) {

    fun updatePositions(positions: DoubleArray) = updateVertices(0, positions)
    fun updateColors(colors: DoubleArray) = updateVertices(1, colors)
    fun updateIndex(index: IntArray) = updateIndices(index)
}