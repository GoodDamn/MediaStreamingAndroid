package good.damn.media.gles.gl.textures

import java.nio.ByteBuffer
import java.nio.IntBuffer

abstract class GLTexture(
    val width: Int,
    val height: Int
) {
    abstract fun drawTexture()
}