package good.damn.media.gles.extensions

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

fun ByteArray.createIndicesBuffer(): ByteBuffer {
    val indicesBuffer = ByteBuffer.allocateDirect(
        size * 2
    ).order(
        ByteOrder.nativeOrder()
    ).put(
        this
    )

    indicesBuffer.position(0)

    return indicesBuffer
}