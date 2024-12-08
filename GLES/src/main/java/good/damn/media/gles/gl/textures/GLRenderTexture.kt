package good.damn.media.gles.gl.textures

import android.content.Context
import android.util.Log
import good.damn.media.gles.GLViewTexture
import good.damn.media.gles.R
import good.damn.media.gles.extensions.rawText
import good.damn.media.gles.gl.interfaces.GLDrawable
import good.damn.media.gles.gl.interfaces.GLLayoutable
import good.damn.media.gles.utils.gl.GLUtilities
import good.damn.media.gles.gl.GL.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

class GLRenderTexture(
    val texture: GLTexture
): GLLayoutable,
GLDrawable {

    var rotation = 0

    private var mUniTexture = 0
    private var mUniResolution = 0
    private var mUniRotation = 0
    private var mTextures = intArrayOf(1)

    private var mViewportWidth = 0f
    private var mViewportHeight = 0f

    fun create(
        program: Int
    ) {

        glGenTextures(
            1,
            mTextures,
            0
        )

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
            "u_rotationDeg"
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

        /*glTexParameteri(
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

        val w = texture.width
        val h = texture.height

        var pixel = 0
        for (ih in 0 until h) {
            for (iw in 0 until w) {
                texture.buffer.apply {
                    put(pixel, 255.toByte()) // A
                    put(pixel+1, 0.toByte()) // R
                    put(pixel+2, 0.toByte()) // G
                    put(pixel+3, 255.toByte()) //  B
                }
                pixel += 4
            }
        }*/

        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGB,
            texture.width,
            texture.height,
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

    override fun layout(
        width: Int,
        height: Int,
        program: Int
    ) {
        mViewportWidth = width.toFloat()
        mViewportHeight = height.toFloat()
    }

    override fun draw(
        program: Int
    ) {
        texture.drawTexture()

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
            mViewportWidth,
            mViewportHeight
        )
    }
}