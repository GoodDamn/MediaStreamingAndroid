package good.damn.editor.mediastreaming.tree

interface BinaryTreeListener<T> {
    fun equality(
        v: T,
        v1: T
    ): Boolean

    fun moreThan(
        v: T,
        v1: T
    ): Boolean
}