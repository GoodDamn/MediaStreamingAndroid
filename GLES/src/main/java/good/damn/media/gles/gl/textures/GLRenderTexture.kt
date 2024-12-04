package good.damn.media.gles.gl.textures

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import good.damn.media.gles.R
import good.damn.media.gles.extensions.rawText
import good.damn.media.gles.gl.interfaces.GLDrawable
import good.damn.media.gles.gl.interfaces.GLLayoutable
import good.damn.media.gles.utils.gl.GLUtilities
import good.damn.media.gles.gl.GL.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.function.IntFunction

class GLRenderTexture(
    program: Int,
    context: Context
): GLLayoutable,
GLDrawable {

    var bitmap: Bitmap? = null

    var rotation = 0

    var width = 0f
    var height = 0f


    private var mBuffer: IntBuffer? = null

    private var mUniTexture = 0
    private var mUniResolution = 0
    private var mUniRotation = 0
    private var mTextures = intArrayOf(1)

    private var mWidth = 0
    private var mHeight = 0

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

        mUniRotation = glGetUniformLocation(
            program,
            "u_rotation"
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

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_SWIZZLE_R,
            GL_GREEN
        )

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_SWIZZLE_G,
            GL_BLUE
        )

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_SWIZZLE_B,
            GL_ALPHA
        )

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_SWIZZLE_A,
            GL_RED
        )


        mWidth = 128
        mHeight = 512

        val pixels = IntArray(
            mWidth * mHeight
        )
        for (ih in 0 until mHeight) {
            for (iw in 0 until mWidth) {
                pixels[ih * mWidth + iw] = 0x00ff00ff // BGRA
            }
        }

        mBuffer = IntBuffer.wrap(
            pixels
        )

        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            mWidth,
            mHeight,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            mBuffer
        )

        glBindTexture(
            GL_TEXTURE_2D,
            0
        )
    }

    override fun draw(
        program: Int
    ) {
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            mWidth,
            mHeight,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            mBuffer
        )

        glActiveTexture(
            GL_TEXTURE0
        )

        glBindTexture(
            GL_TEXTURE_2D,
            mTextures[0]
        )

        glUniform1i(
            mUniRotation,
            rotation
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