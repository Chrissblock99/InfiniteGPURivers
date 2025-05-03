package me.chriss99.erosion

import me.chriss99.Area

class ErosionTask(
    val eroder: GPUTerrainEroder, val area: Area, val steps: Int,
    val l: Int, val r: Int, val f: Int, val b: Int
) {
    var currentArea = area.increase(
            if (r == 0) 0 else -steps,
            if (f == 0) 0 else -steps,
            if (l == 0) 0 else -steps,
            if (b == 0) 0 else -steps
        )
        private set
    var currentStep = 0
        private set

    fun erosionStep(): Boolean {
        if (isDone)
            return true

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

    val hasStarted get() = currentStep != 0
    val isDone get() = currentStep >= steps
    val isRunning get() = hasStarted && !isDone

    val nextIterations get() = currentArea.area
    val newSlopes: Int get() {
        var sum = 0

        sum += if (l == 0) 1 else 0
        sum += if (r == 0) 1 else 0
        sum += if (f == 0) 1 else 0
        sum += if (b == 0) 1 else 0

        return sum
    }
}