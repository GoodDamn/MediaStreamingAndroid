package good.damn.media.gles.gl.quad

import android.content.Context
import good.damn.media.gles.R
import good.damn.media.gles.extensions.createFloatBuffer
import good.damn.media.gles.extensions.createIndicesBuffer
import good.damn.media.gles.extensions.rawText
import good.damn.media.gles.gl.GL.*
import good.damn.media.gles.gl.interfaces.GLDrawable
import good.damn.media.gles.gl.interfaces.GLLayoutable
import good.damn.media.gles.utils.gl.GLUtilities

class GLRenderQuad
: GLDrawable,
GLLayoutable {

    companion object {
        private const val STRIDE = 8
        private const val SIZE = 2
    }

    private val mIndices = byteArrayOf(
        0, 1, 2,
        0, 2, 3
    ).createIndicesBuffer()

    private val mQuadCoords = floatArrayOf(
        -1.0f, 1.0f,  // top left
        -1.0f, -1.0f, // bottom left
        1.0f, -1.0f,  // bottom right
        1.0f, 1.0f,   // top right
    ).createFloatBuffer()

    private var mAttrPosition = 0

    fun create(
        program: Int
    ) {

        mAttrPosition = glGetAttribLocation(
            program,
            "position"
        )
    }

    override fun layout(
        width: Int,
        height: Int,
        program: Int
    ) {}

    override fun draw(
        program: Int
    ) {

        glEnableVertexAttribArray(
            mAttrPosition
        )

        glVertexAttribPointer(
            mAttrPosition,
            SIZE,
            GL_FLOAT,
            false,
            STRIDE,
            mQuadCoords
        )

        glDrawElements(
            GL_TRIANGLES,
            mIndices.capacity(),
            GL_UNSIGNED_BYTE,
            mIndices
        )

        glDisableVertexAttribArray(
            mAttrPosition
        )
    }

}