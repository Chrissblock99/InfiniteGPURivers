package me.chriss99.worldmanagement.iteration

enum class IterationSurface(val l: Int, val r: Int, val f: Int, val b: Int,
                            private val tlStr: String, private val trStr: String, private val dlStr: String, private val drStr: String,
                            val tlElev: Int, val trElev: Int, val dlElev: Int, val drElev: Int) {
    FLAT(0, 0, 0, 0, "FLAT", "FLAT", "FLAT", "FLAT", 0, 0, 0, 0),
    SLOPE_LEFT(0, 0, -1, -1, "SLOPE_LEFT", "SLOPE_LEFT", "SLOPE_LEFT", "SLOPE_LEFT", 0, 1, 0, 1),
    SLOPE_RIGHT(0, 0, 1, 1, "SLOPE_RIGHT", "SLOPE_RIGHT", "SLOPE_RIGHT", "SLOPE_RIGHT", 1, 0, 1, 0),
    SLOPE_FORWARD(1, 1, 0, 0, "SLOPE_FORWARD", "SLOPE_FORWARD", "SLOPE_FORWARD", "SLOPE_FORWARD", 0, 0, 1, 1),
    SLOPE_BACKWARD(-1, -1, 0, 0, "SLOPE_BACKWARD", "SLOPE_BACKWARD", "SLOPE_BACKWARD", "SLOPE_BACKWARD", 1, 1, 0, 0),
    OUTWARD_TOP_LEFT(0, 1, 0, -1, "OUTWARD_TOP_LEFT", "SLOPE_FORWARD", "SLOPE_LEFT", "OUTWARD_TOP_LEFT", 0, 0, 0, 1),
    OUTWARD_TOP_RIGHT(1, 0, 0, 1, "SLOPE_FORWARD", "OUTWARD_TOP_RIGHT", "OUTWARD_TOP_RIGHT", "SLOPE_RIGHT", 0, 0, 1, 0),
    OUTWARD_DOWN_LEFT(0, -1, -1, 0, "SLOPE_LEFT", "OUTWARD_DOWN_LEFT", "OUTWARD_DOWN_LEFT", "SLOPE_BACKWARD", 0, 1, 0, 0),
    OUTWARD_DOWN_RIGHT(-1, 0, 1, 0, "OUTWARD_DOWN_RIGHT", "SLOPE_RIGHT", "SLOPE_BACKWARD", "OUTWARD_DOWN_RIGHT",1, 0, 0, 0),
    INWARD_TOP_LEFT(1, 0, -1, 0, "INWARD_TOP_LEFT", "SLOPE_LEFT", "SLOPE_FORWARD", "INWARD_TOP_LEFT", 0, 1, 1, 1),
    INWARD_TOP_RIGHT(0, 1, 1, 0, "SLOPE_RIGHT", "INWARD_TOP_RIGHT", "INWARD_TOP_RIGHT", "SLOPE_FORWARD", 1, 0, 1, 1),
    INWARD_DOWN_LEFT(-1, 0, 0, -1, "SLOPE_BACKWARD", "INWARD_DOWN_LEFT", "INWARD_DOWN_LEFT", "SLOPE_LEFT", 1, 1, 0, 1),
    INWARD_DOWN_RIGHT(0, -1, 0, 1, "INWARD_DOWN_RIGHT", "SLOPE_BACKWARD", "SLOPE_RIGHT", "INWARD_DOWN_RIGHT", 1, 1, 1, 0);

    val tl get() = valueOf(tlStr)
    val tr get() = valueOf(trStr)
    val dl get() = valueOf(dlStr)
    val dr get() = valueOf(drStr)

    companion object {
        private val parents = mapOf(*entries.map { (with(it) { tl to tr to dl to dr} ) to it }.toTypedArray())

        fun hasParent(tl: IterationSurface, tr: IterationSurface, dl: IterationSurface, dr: IterationSurface) = parents[tl to tr to dl to dr]
    }
}