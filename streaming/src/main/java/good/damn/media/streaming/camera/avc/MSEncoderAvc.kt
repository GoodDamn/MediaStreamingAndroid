package good.damn.media.streaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.media.streaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MSEncoderAvc
: MSCoder(),
    MSStateable {

    companion object {
        private const val TAG = "MSEncoderAvc"
        private const val TIMEOUT_USAGE_MCRS = 33_000L
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createEncoderByType(
        TYPE_AVC
    )

    private val mBufferInfo = MediaCodec.BufferInfo()

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
            while (isRunning) {
                try {
                    val status = mCoder.dequeueOutputBuffer(
                        mBufferInfo,
                        TIMEOUT_USAGE_MCRS
                    )

                    when (status) {
                        MediaCodec.INFO_TRY_AGAIN_LATER -> {
                            if (!isRunning) {
                                break
                            }
                        }

                        in Int.MIN_VALUE until 0 -> {
                        }

                        else -> {
                            processEncodingBuffers(
                                status
                            )
                        }
                    }
                } catch (_: Exception) { }
            }
        }
    }

    private inline fun processEncodingBuffers(
        id: Int
    ) {
        val buffer = mCoder.getOutputBuffer(
            id
        ) ?: return

        mRemaining = buffer.remaining()
        
        onGetFrameData?.onGetFrameData(
            buffer,
            0,
            mRemaining
        )

        mCoder.releaseOutputBuffer(
            id,
            false
        )
    }
}