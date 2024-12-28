package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.camera.avc.listeners.MSListenerOnGetFrameData
import java.io.ByteArrayOutputStream

class MSDecoderAvc(
    width: Int,
    height: Int,
    rotation: Int,
    surfacePreview: Surface
): MediaCodec.Callback(),
MSListenerOnGetFrameData {

    companion object {
        private const val TAG = "MSDecoderAvc"
    }

    private var mBuffer = ByteArray(0)

    private val mStream = ByteArrayOutputStream()

    // may throws Exception with no h264 codec
    private val mDecoder = MediaCodec.createDecoderByType(
        MSEncoderAvc.TYPE_AVC
    ).apply {

        val format = MediaFormat.createVideoFormat(
            MSEncoderAvc.TYPE_AVC,
            width,
            height
        ).apply {
            setInteger(
                MediaFormat.KEY_ROTATION,
                rotation
            )
        }

        configure(
            format,
            surfacePreview,
            null,
            0
        )

        setOutputSurface(
            surfacePreview
        )

        setCallback(
            this@MSDecoderAvc
        )
    }

    fun start() {
        mDecoder.start()
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