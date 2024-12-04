package good.damn.media.gles.gl.textures

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLUtils
import good.damn.media.gles.R
import good.damn.media.gles.extensions.rawText
import good.damn.media.gles.gl.interfaces.GLDrawable
import good.damn.media.gles.gl.interfaces.GLLayoutable
import good.damn.media.gles.utils.gl.GLUtilities
import good.damn.media.gles.gl.GL.*

class GLRenderTexture(
    program: Int,
    context: Context
): GLLayoutable,
GLDrawable {

    var bitmap: Bitmap? = null

    var width = 0f
    var height = 0f

    private var mUniTexture = 0
    private var mUniResolution = 0
    private var mTextures = intArrayOf(1)

    init {
        glAttachShader(
            program,
            GLUtilities.loadShader(
                GL_FRAGMENT_SHADER,
                context.rawText(
                    R.raw.frag
                )
            )
        )

        glGenTextures(
            1,
            mTextures,
            0
        )
    }

    override fun layout(
        width: Int,
        height: Int,
        program: Int
    ) {
        this.width = width.toFloat()
        this.height = height.toFloat()

        mUniTexture = glGetUniformLocation(
            program,
            "u_tex"
        )

        mUniResolution = glGetUniformLocation(
            program,
            "u_res"
        )

        glBindTexture(
            GL_TEXTURE_2D,
            mTextures[0]
        )

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            GL_LINEAR
        )

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR
        )

        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGB,
            width,
            height,
            0,
            GL_RGB,
            GL_UNSIGNED_BYTE,
            null
        )

        glBindTexture(
            GL_TEXTURE_2D,
            0
        )
    }

    override fun draw(
        program: Int
    ) {
        bitmap?.apply {
            GLUtils.texImage2D(
                GL_TEXTURE_2D,
                0,
                this,
                0
            )
        }

        glActiveTexture(
            GL_TEXTURE0
        )

        glBindTexture(
            GL_TEXTURE_2D,
            mTextures[0]
        )

        glUniform1i(
            mUniTexture,
            0
        )

        glUniform2f(
            mUniResolution,
            width,
            height
        )
    }
}