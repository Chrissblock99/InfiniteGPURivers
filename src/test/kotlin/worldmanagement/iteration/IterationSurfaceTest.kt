package worldmanagement.iteration

import me.chriss99.worldmanagement.iteration.IterationSurface
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IterationSurfaceTest {
    @Test
    fun checkDataConsistency() {
        IterationSurface.entries.forEach {
            //borders
            assertEquals(it.l, it.tl.l, it.name)
            assertEquals(it.f, it.tl.f, it.name)
            assertEquals(it.r, it.tr.r, it.name)
            assertEquals(it.f, it.tr.f, it.name)
            assertEquals(it.l, it.dl.l, it.name)
            assertEquals(it.b, it.dl.b, it.name)
            assertEquals(it.r, it.dr.r, it.name)
            assertEquals(it.b, it.dr.b, it.name)

            //inner
            assertTrue(it.tl.r == it.tr.l, it.name)
            assertTrue(it.dl.r == it.dr.l, it.name)
            assertTrue(it.tl.b == it.dl.f, it.name)
            assertTrue(it.tr.b == it.dr.f, it.name)
        }
    }
}