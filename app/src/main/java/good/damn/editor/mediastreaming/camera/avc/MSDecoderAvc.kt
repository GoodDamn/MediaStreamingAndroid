package good.damn.editor.mediastreaming.camera.avc

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import good.damn.editor.mediastreaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.editor.mediastreaming.network.MSStateable
import java.io.ByteArrayOutputStream

class MSDecoderAvc
: MSCoder(),
MSListenerOnGetFrameData,
MSStateable {

    companion object {
        private const val TAG = "MSDecoderAvc"
    }

    private var mBuffer = ByteArray(0)

    private val mStream = ByteArrayOutputStream()

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) = mCoder.run {
        setCallback(
            this@MSDecoderAvc
        )
        configure(
            format,
            decodeSurface,
            null,
            0
        )
    }

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        Log.d(TAG, "onInputBufferAvailable: $index")

        if (isUnitialized) {
            return
        }

        val inp = codec.getInputBuffer(
            index
        ) ?: return

        inp.clear()

        mBuffer = mStream.toByteArray()
        mStream.reset()

        if (mBuffer.size > inp.capacity()) {
            return
        }

        inp.put(
            mBuffer,
            0,
            mBuffer.size
        )

        codec.queueInputBuffer(
            index,
            0,
            mBuffer.size,
            0,
            0
        )
    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        Log.d(TAG, "onOutputBufferAvailable: $index")

        if (isUnitialized) {
            return
        }

        codec.getOutputBuffer(
            index
        )

        codec.releaseOutputBuffer(
            index,
            true
        )
    }

    override fun onError(
        codec: MediaCodec,
        e: MediaCodec.CodecException
    ) {
        Log.d(TAG, "onError: ${e.localizedMessage}")
    }

    override fun onOutputFormatChanged(
        codec: MediaCodec,
        format: MediaFormat
    ) {
        Log.d(TAG, "onOutputFormatChanged: $format")
    }

    override fun onGetFrameData(
        bufferData: ByteArray,
        offset: Int,
        len: Int
    ) {
        mStream.write(
            bufferData,
            offset,
            len
        )
    }

}