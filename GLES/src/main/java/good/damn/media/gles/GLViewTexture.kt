package good.damn.media.gles

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import good.damn.media.gles.gl.renderers.GLRendererFrame
import good.damn.media.gles.gl.GL.*

class GLViewTexture(
    context: Context
): GLSurfaceView(
    context
) {

    var bitmap: Bitmap?
        get() = mRenderer.bitmap
        set(v) {
            mRenderer.bitmap = v
            requestRender()
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