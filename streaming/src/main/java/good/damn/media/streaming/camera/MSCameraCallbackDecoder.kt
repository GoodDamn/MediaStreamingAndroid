package good.damn.media.streaming.camera

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import good.damn.media.streaming.MSStreamConstantsPacket
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.extensions.short
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

class MSCameraCallbackDecoder
: MediaCodec.Callback() {

    companion object {
        private const val TAG = "MSCameraCallbackDecoder"
    }

    private val mQueueFrames = ConcurrentLinkedQueue<
        MSFrame
    >()

    private var mSizeFrame = 0
    private var mSizePacket = 0
    private var mInputBuffer: ByteBuffer? = null

    fun addFrame(
        frame: MSFrame
    ) = mQueueFrames.add(
        frame
    )

    fun clearQueue() = mQueueFrames.clear()

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        try {
            processInputBuffer(
                codec,
                index
            )
        } catch (e: Exception) {

        }
    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        try {
            processOutputBuffer(
                index,
                codec
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

    private inline fun processOutputBuffer(
        index: Int,
        codec: MediaCodec
    ) {
        codec.releaseOutputBuffer(
            index,
            true
        )
    }

    private inline fun processInputBuffer(
        codec: MediaCodec,
        index: Int
    ) {
        mInputBuffer = codec.getInputBuffer(
            index
        )

        if (mInputBuffer == null) {
            Log.d(TAG, "processInputBuffer: NULL")
            return
        }

        mInputBuffer!!.clear()
        mSizeFrame = 0

        if (mQueueFrames.isNotEmpty()) {
            Log.d(TAG, "processInputBuffer: ")
            mQueueFrames.remove().packets.forEach {
                it?.apply {
                    mSizePacket = data.short(
                        MSStreamConstantsPacket.OFFSET_PACKET_SIZE
                    )

                    mInputBuffer!!.put(
                        data,
                        MSStreamConstantsPacket.LEN_META,
                        mSizePacket
                    )

                    mSizeFrame += mSizePacket
                }
            }
        }
        
        codec.queueInputBuffer(
            index,
            0,
            mSizeFrame,
            0,
            0
        )
    }

}