package good.damn.media.gles

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import good.damn.media.gles.gl.renderers.GLRendererFrame

class GLViewTexture(
    context: Context
): GLSurfaceView(
    context
) {

    var bitmap: Bitmap?
        get() = mRenderer.bitmap
        set(v) {
            mRenderer.bitmap = v
        }

    var rotationShade: Int
        get() = mRenderer.rotation
        set(v) {
            mRenderer.rotation = v
        }

    private val mRenderer = GLRendererFrame(
        context
    )

    init {
        setEGLContextClientVersion(2)

        setRenderer(
            mRenderer
        )
        renderMode = RENDERMODE_WHEN_DIRTY

    }
}