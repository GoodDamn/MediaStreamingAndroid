package good.damn.media.streaming.camera

import android.media.MediaCodec
import android.util.Log
import good.damn.media.streaming.MSStreamConstantsPacket
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.extensions.short
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

class MSCameraCodecBuffers {

    companion object {
        private const val TAG = "MSCameraCodecBuffers"
    }

    private val mQueueFrames = ConcurrentLinkedQueue<
        MSFrame
    >()

    private val mQueueAvailableBuffers = ConcurrentLinkedQueue<Int>()

    private var mSizeFrame = 0
    private var mSizePacket = 0
    private var mInputBuffer: ByteBuffer? = null

    fun addFrame(
        frame: MSFrame
    ) = mQueueFrames.add(
        frame
    )

    fun addAvailableBufferIndex(
        index: Int
    ) = mQueueAvailableBuffers.add(
        index
    )

    fun clearQueue() = mQueueFrames.clear()

    fun showNextFrame(
        codec: MediaCodec
    ) {
        if (mQueueAvailableBuffers.isEmpty() || mQueueFrames.isEmpty()) {
            return
        }

        val index = mQueueAvailableBuffers.remove()

        mInputBuffer = codec.getInputBuffer(
            index
        )

        if (mInputBuffer == null) {
            Log.d(TAG, "processInputBuffer: NULL")
            return
        }

        mInputBuffer!!.clear()
        mSizeFrame = 0

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

        codec.queueInputBuffer(
            index,
            0,
            mSizeFrame,
            0,
            0
        )
    }

}