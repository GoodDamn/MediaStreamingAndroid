package good.damn.editor.mediastreaming

import good.damn.editor.mediastreaming.tree.BinaryTree
import good.damn.editor.mediastreaming.tree.BinaryTreeListener
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private val tree = BinaryTree(
        object: BinaryTreeListener<Int> {
            override fun equality(
                v: Int,
                v1: Int
            ) = v == v1

            override fun moreThan(
                v: Int,
                v1: Int
            ) = v > v1
        }
    ).apply {
        add(4)
        add(3)
        add(1)
        add(2)
        add(7)
    }

    @Test
    fun binaryTree_first() {
        val first = tree.getFirst()
            ?: assert(false)

        assertEquals(
            first,
            4
        )
    }
}