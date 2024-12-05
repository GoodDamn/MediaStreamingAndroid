package good.damn.media.gles

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import good.damn.media.gles.gl.renderers.GLRendererFrame
import good.damn.media.gles.gl.textures.GLTexture

class GLViewTexture(
    context: Context,
    texture: GLTexture
): GLSurfaceView(
    context
) {

    var rotationShade: Int
        get() = mRenderer.rotation
        set(v) {
            mRenderer.rotation = v
        }

    private val mRenderer = GLRendererFrame(
        context,
        texture
    )

    init {
        setEGLContextClientVersion(2)

        setRenderer(
            mRenderer
        )

        renderMode = RENDERMODE_WHEN_DIRTY

    }
}