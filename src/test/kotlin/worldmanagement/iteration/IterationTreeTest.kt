package worldmanagement.iteration

import me.chriss99.Area
import me.chriss99.worldmanagement.iteration.IterationSurface
import me.chriss99.worldmanagement.iteration.IterationTree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IterationTreeTest {
    @Test
    fun idk() {
        IterationSurface.entries.forEach {
            val tree = IterationTree.IterationLeaf(Area(2), 128, it)
            assertEquals(tree, tree.subDivide().attemptMerge(), it.name)
        }
    }
}