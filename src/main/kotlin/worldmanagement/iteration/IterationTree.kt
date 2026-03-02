package me.chriss99.worldmanagement.iteration

import glm_.vec2.Vec2i
import me.chriss99.Area

sealed class IterationTree(val area: Area, val iteration: Int) {
    abstract operator fun get(pos: Vec2i): IterationTile


    class IterationQuad(area: Area, iteration: Int, var tl: IterationTree, var tr: IterationTree, var dl: IterationTree, var dr: IterationTree) : IterationTree(area, iteration) {
        override fun get(pos: Vec2i): IterationTile {
            TODO("Not yet implemented")
        }

        fun attemptMerge(): IterationLeaf? {
            if (!(tl is IterationLeaf && tr is IterationLeaf && dl is IterationLeaf && dr is IterationLeaf))
                return null

            return IterationLeaf(area, iteration, IterationSurface.hasParent((tl as IterationLeaf).surface, (tr as IterationLeaf).surface, (dl as IterationLeaf).surface, (dr as IterationLeaf).surface) ?: return null)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is IterationQuad) return false

            if (area != other.area) return false
            if (tl != other.tl) return false
            if (tr != other.tr) return false
            if (dl != other.dl) return false
            if (dr != other.dr) return false

            return true
        }

        override fun hashCode(): Int {
            var result = area.hashCode()
            result = 31 * result + tl.hashCode()
            result = 31 * result + tr.hashCode()
            result = 31 * result + dl.hashCode()
            result = 31 * result + dr.hashCode()
            return result
        }
    }
    class IterationLeaf(area: Area, iteration: Int, var surface: IterationSurface) : IterationTree(area, iteration) {
        override fun get(pos: Vec2i): IterationTile {
            TODO("Not yet implemented")
        }

        fun subDivide(): IterationQuad {
            val halfSize = area.size.x / 2
            val halfSizeArea = Area(halfSize)
            val pos: Vec2i = area.srcPos
            return IterationQuad(area, iteration,
                IterationLeaf(halfSizeArea + pos + Vec2i(0, halfSize), iteration + halfSize*surface.tlElev, surface.tl),
                IterationLeaf(halfSizeArea + pos + Vec2i(halfSize), iteration + halfSize*surface.trElev, surface.tr),
                IterationLeaf(halfSizeArea + pos, iteration + halfSize*surface.dlElev, surface.dl),
                IterationLeaf(halfSizeArea + pos + Vec2i(halfSize, 0), iteration + halfSize*surface.drElev, surface.dr)
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is IterationLeaf) return false

            if (area != other.area) return false
            if (surface != other.surface) return false

            return true
        }

        override fun hashCode(): Int {
            var result = area.hashCode()
            result = 31 * result + surface.hashCode()
            return result
        }
    }
}