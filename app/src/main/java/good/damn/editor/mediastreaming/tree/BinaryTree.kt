package good.damn.editor.mediastreaming.tree

import java.util.LinkedList

class BinaryTree<T>(
    val listener: BinaryTreeListener<T>
) {
    private var root: Node<T>? = null

    fun add(
        element: T
    ) {
        if (root == null) {
            root = Node(
                element
            )
            return
        }

        searchAdd(
            root,
            element
        )
    }

    fun isEmpty() = root == null

    fun getFirst() = first(root)?.data

    fun forEach(
        node: Node<T>? = root,
        action: (T) -> Boolean
    ) {
        if (node == null) {
            return
        }

        forEach(
            node.leftNode,
            action
        )

        if (action.invoke(
            node.data
        )) return

        forEach(
            node.rightNode,
            action
        )
    }

    inner class Node<T>(
        var data: T,
        var leftNode: Node<T>? = null,
        var rightNode: Node<T>? = null
    )
}

fun <T> BinaryTree<T>.toList()
    : List<T> = LinkedList<T>().apply {
    this@toList.forEach { add(it) }
}

private fun <T> BinaryTree<T>.first(
    node: BinaryTree<T>.Node<T>? = null
): BinaryTree<T>.Node<T>? {
    if (node == null) {
        return null
    }

    return first(
        node.leftNode
    ) ?: node
}

private fun <T> BinaryTree<T>.searchh(
    node: BinaryTree<T>.Node<T>? = null,
    data: T
): BinaryTree<T>.Node<T>? {
    if (node == null) {
        return null
    }

    if (listener.moreThan(data, node.data)) {
        if (node.rightNode == null) {
            return null
        }

        return searchh(
            node.rightNode,
            data
        ) ?: node
    }

    if (node.leftNode == null) {
        return null
    }

    return searchh(
        node.leftNode,
        data
    ) ?: node
}

private fun <T> BinaryTree<T>.searchAdd(
    node: BinaryTree<T>.Node<T>? = null,
    data: T
) {
    if (node == null) {
        return
    }

    if (listener.moreThan(data, node.data)) {
        if (node.rightNode == null) {
            node.rightNode = Node(
                data
            )
            return
        }

        searchAdd(
            node.rightNode,
            data
        )
        return
    }

    if (node.leftNode == null) {
        node.leftNode = Node(
            data
        )
        return
    }

    searchAdd(
        node.leftNode,
        data
    )
}