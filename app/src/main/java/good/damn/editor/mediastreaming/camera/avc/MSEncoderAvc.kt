package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import android.util.Log
import good.damn.editor.mediastreaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.editor.mediastreaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        configure(
            format,
            null,
            null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )
    }

    fun createInputSurface() = mCoder.createInputSurface()

    override fun start() {
        super.start()

        CoroutineScope(
            Dispatchers.IO
        ).launch {
            while (!isUninitialized) {
                processEncodingBuffers()
            }
        }
    }

    private inline fun processEncodingBuffers() {

        val outId = mCoder.dequeueOutputBuffer(
            MediaCodec.BufferInfo(),
            -1
        )

        if (outId >= 0) {
            val buffer = mCoder.getOutputBuffer(
                outId
            ) ?: return

            Log.d(TAG, "processEncodingBuffers: $outId ")

            mRemaining = buffer.remaining()

            mFrame = ByteArray(
                mRemaining
            )

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

            mCoder.releaseOutputBuffer(
                outId,
                false
            )
        }
    }
}