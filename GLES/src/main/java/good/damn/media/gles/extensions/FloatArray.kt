package good.damn.media.gles.extensions

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun FloatArray.createFloatBuffer(): FloatBuffer {
    val vertexBuffer = ByteBuffer.allocateDirect(
        size * 4
    ).order(
        ByteOrder.nativeOrder()
    ).asFloatBuffer().put(this)

    vertexBuffer.position(0)

    return vertexBuffer
}