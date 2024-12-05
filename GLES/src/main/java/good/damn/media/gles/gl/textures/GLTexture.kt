package good.damn.media.gles.gl.textures

import java.nio.ByteBuffer
import java.nio.IntBuffer

data class GLTexture(
    val width: Int,
    val height: Int,
    val buffer: ByteBuffer = ByteBuffer.allocateDirect(
        width * height * 4
    )
)