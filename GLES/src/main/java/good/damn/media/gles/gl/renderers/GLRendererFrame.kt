package good.damn.media.gles.gl.renderers

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import good.damn.media.gles.R
import good.damn.media.gles.extensions.rawText
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import good.damn.media.gles.gl.quad.GLRenderQuad
import good.damn.media.gles.gl.textures.GLRenderTexture
import good.damn.media.gles.gl.GL.*
import good.damn.media.gles.gl.textures.GLTexture
import good.damn.media.gles.utils.gl.GLUtilities

class GLRendererFrame(
    private val context: Context,
    texture: GLTexture
): GLSurfaceView.Renderer {

    var rotation: Int
        get() = mTexture.rotation
        set(v) {
            mTexture.rotation = v
        }

    private val mQuad = GLRenderQuad()
    private val mTexture = GLRenderTexture(
        texture
    )

    private var mProgram = 0

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        mProgram = glCreateProgram()

        glAttachShader(
            mProgram,
            GLUtilities.loadShader(
                GL_VERTEX_SHADER,
                context.rawText(
                    R.raw.vert
                )
            )
        )

        glAttachShader(
            mProgram,
            GLUtilities.loadShader(
                GL_FRAGMENT_SHADER,
                context.rawText(
                    R.raw.frag
                )
            )
        )

        glLinkProgram(
            mProgram
        )

        glUseProgram(
            mProgram
        )

        mQuad.create(
            mProgram
        )

        mTexture.create(
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
        mTexture.draw(
            mProgram
        )

        mQuad.draw(
            mProgram
        )

    }


}