package me.chriss99.erosion

import me.chriss99.Area

class ErosionTask(
    eroder: GPUTerrainEroder, area: Area,
    private val steps: Int,
    val l: Int, val r: Int, val f: Int, val b: Int
) {
    private val eroder: GPUTerrainEroder = eroder

    private val area = area.copy()

    private var currentArea: Area
    private var currentStep: Int

    init {
        currentArea = area.increase(
            if (r == 0) 0 else -steps,
            if (f == 0) 0 else -steps,
            if (l == 0) 0 else -steps,
            if (b == 0) 0 else -steps
        )
        currentStep = 0
    }

    fun erosionStep(): Boolean {
        if (isDone) return true

        eroder.erode(currentArea)
        currentArea = currentArea.increase(
            if (r == 0) -1 else 1,
            if (f == 0) -1 else 1,
            if (l == 0) -1 else 1,
            if (b == 0) -1 else 1
        )
        currentStep++
        return false
    }

    fun hasStarted(): Boolean {
        return currentStep != 0
    }

    val isDone: Boolean
        get() = currentStep >= steps

    val isRunning: Boolean
        get() = hasStarted() && !isDone

    fun nextIterations(): Int {
        return area.area
    }

    fun newSlopes(): Int {
        var sum = 0

        sum += if (l == 0) 1 else 0
        sum += if (r == 0) 1 else 0
        sum += if (f == 0) 1 else 0
        sum += if (b == 0) 1 else 0

        return sum
    }

    fun getArea(): Area {
        return area.copy()
    }
}