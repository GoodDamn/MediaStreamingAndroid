package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.provider.MediaStore.Audio.Media
import android.util.Log
import good.damn.editor.mediastreaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.editor.mediastreaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class MSEncoderAvc
: MSCoder(),
MSStateable {

    companion object {
        private const val TAG = "MSEncoderAvc"
        private const val TIMEOUT_USAGE_MS = 10000L
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
            while (true) {
                if (!isRunning) {
                    mCoder.signalEndOfInputStream()
                    break
                }

                while (true) {
                    val status = mCoder.dequeueOutputBuffer(
                        mBufferInfo,
                        TIMEOUT_USAGE_MS
                    )

                    when (status) {
                        MediaCodec.INFO_TRY_AGAIN_LATER -> {
                            Log.d(TAG, "start: TRY_AGAIN")
                            if (!isRunning) {
                                break
                            }
                        }

                        in Int.MIN_VALUE until 0 -> {
                            Log.d(TAG, "start: WAITING")
                        }

                        else -> {
                            processEncodingBuffers(
                                status
                            )
                        }
                    }
                }
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

        val mFrame = ByteArray(
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
            id,
            false
        )
    }
}