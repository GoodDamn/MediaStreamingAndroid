package good.damn.media.gles.gl.renderers

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import good.damn.media.gles.gl.quad.GLRenderQuad
import good.damn.media.gles.gl.textures.GLRenderTexture
import good.damn.media.gles.gl.GL.*

class GLRendererFrame(
    private val context: Context
): GLSurfaceView.Renderer {

    var bitmap: Bitmap?
        get() = mTexture.bitmap
        set(v) {
            mTexture.bitmap = v
        }

    private lateinit var mQuad: GLRenderQuad
    private lateinit var mTexture: GLRenderTexture

    private var mProgram = 0

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        mProgram = glCreateProgram()

        mQuad = GLRenderQuad(
            mProgram,
            context
        )

        mTexture = GLRenderTexture(
            mProgram,
            context
        )

        glLinkProgram(
            mProgram
        )

        glUseProgram(
            mProgram
        )
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int
    ) {
        mQuad.layout(
            width,
            height,
            mProgram
        )

        mTexture.layout(
            width,
            height,
            mProgram
        )
    }

    override fun onDrawFrame(
        gl: GL10?
    ) {
        glClear(
            GL_COLOR_BUFFER_BIT or
            GL_DEPTH_BUFFER_BIT
        )

        glClearColor(
            1.0f,
            0.0f,
            0.0f,
            1.0f
        )

        mTexture.draw(
            mProgram
        )

        mQuad.draw(
            mProgram
        )

    }


}