package good.damn.media.gles.gl.textures

import android.graphics.Bitmap
import android.opengl.GLUtils
import good.damn.media.gles.gl.GL

class GLTextureBitmap(
    width: Int,
    height: Int
): GLTexture(
    width,
    height
) {
    var bitmap: Bitmap? = null

    override fun drawTexture() {
        bitmap?.apply {
            GLUtils.texImage2D(
                GL.GL_TEXTURE_2D,
                0,
                this,
                0
                )
        }
    }

}