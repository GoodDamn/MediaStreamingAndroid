package good.damn.media.gles.gl.textures

import android.graphics.Bitmap
import android.opengl.GLES30.GL_RGBA
import android.opengl.GLES30.GL_TEXTURE_2D
import android.opengl.GLES30.GL_UNSIGNED_BYTE
import android.opengl.GLES30.glTexImage2D
import android.opengl.GLUtils
import good.damn.media.gles.gl.GL
import java.nio.ByteBuffer

class GLTextureBuffer(
    width: Int,
    height: Int,
    private val buffer: ByteBuffer = ByteBuffer.allocateDirect(
        width * height * 4
    )
): GLTexture(
    width,
    height
) {

    override fun drawTexture() {
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            width,
            height,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            buffer
        )
    }

}