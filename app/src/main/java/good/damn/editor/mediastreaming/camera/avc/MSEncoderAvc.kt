package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import good.damn.editor.mediastreaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.editor.mediastreaming.network.MSStateable

class MSEncoderAvc
: MSCoder(),
MSStateable {

    companion object {
        private const val TAG = "MSEncoderAvc"
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createEncoderByType(
        TYPE_AVC
    )

    private var mFrame = ByteArray(0)
    private var mRemaining = 0

    var onGetFrameData: MSListenerOnGetFrameData? = null

    fun configure(
        format: MediaFormat
    ) = mCoder.run {
        setCallback(
            this@MSEncoderAvc
        )

        configure(
            format,
            null,
            null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )
    }

    fun createInputSurface() = mCoder.createInputSurface()

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        Log.d(TAG, "onInputBufferAvailable: ")
    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        if (isUninitialized) {
            return
        }

        val buffer = codec.getOutputBuffer(
            index
        ) ?: return

        Log.d(TAG, "onOutputBufferAvailable: $index ${info.size} ${buffer.capacity()}")

        if (info.size > mFrame.size) {
            mFrame = ByteArray(
                info.size
            )
        }

        mRemaining = buffer.remaining()

        buffer.get(
            mFrame,
            0,
            mRemaining
        )

        onGetFrameData?.onGetFrameData(
            mFrame,
            0,
            mRemaining
        )

        codec.releaseOutputBuffer(
            index,
            false
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

}