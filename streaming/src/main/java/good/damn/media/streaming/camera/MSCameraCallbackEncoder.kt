package good.damn.media.streaming.camera

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData

class MSCameraCallbackEncoder
: MediaCodec.Callback() {

    companion object {
        private const val TAG = "MSCameraCallbackEncoder"
    }

    private var mRemaining = 0

    var onGetFrameData: MSListenerOnGetFrameData? = null

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) = Unit

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        try {
            val buffer = codec.getOutputBuffer(
                index
            ) ?: return

            mRemaining = buffer.remaining()

            onGetFrameData?.onGetFrameData(
                buffer,
                0,
                mRemaining
            )

            codec.releaseOutputBuffer(
                index,
                false
            )
        } catch (e: Exception) {

        }
    }

    override fun onError(
        codec: MediaCodec,
        e: MediaCodec.CodecException
    ) = Unit

    override fun onOutputFormatChanged(
        codec: MediaCodec,
        format: MediaFormat
    ) = Unit
}