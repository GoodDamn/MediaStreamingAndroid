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
import java.io.ByteArrayOutputStream

class MSDecoderAvc(
    width: Int,
    height: Int,
    rotation: Int
): MediaCodec.Callback(),
MSListenerOnGetFrameData {

    companion object {
        private const val TAG = "MSDecoderAvc"
    }

    private var mBuffer = ByteArray(0)

    private val mStream = ByteArrayOutputStream()

    private val mFormat = MediaFormat.createVideoFormat(
        MSEncoderAvc.TYPE_AVC,
        width,
        height
    ).apply {
        setInteger(
            MediaFormat.KEY_ROTATION,
            rotation
        )
    }

    // may throws Exception with no h264 codec
    private val mDecoder = MediaCodec.createDecoderByType(
        MSEncoderAvc.TYPE_AVC
    ).apply {
        setCallback(
            this@MSDecoderAvc
        )
    }

    fun start() {
        mDecoder.start()
    }

    fun configure(
        decodeSurface: Surface
    ) {
        mDecoder.configure(
            mFormat,
            decodeSurface,
            null,
            0
        )
    }

    fun release() {
        mDecoder.release()
    }

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        Log.d(TAG, "onInputBufferAvailable: $index")

        val inp = codec.getInputBuffer(
            index
        ) ?: return

        inp.clear()

        mBuffer = mStream.toByteArray()
        mStream.reset()

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