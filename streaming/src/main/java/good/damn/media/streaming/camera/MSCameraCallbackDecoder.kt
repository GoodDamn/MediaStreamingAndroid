package good.damn.media.streaming.camera

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.extensions.short
import java.util.concurrent.ConcurrentLinkedQueue

class MSCameraCallbackDecoder
: MediaCodec.Callback() {

    companion object {
        private const val TAG = "MSCameraCallbackDecoder"
    }

    private val mQueueFrames = ConcurrentLinkedQueue<
        MSFrame
    >()

    fun addFrame(
        frame: MSFrame
    ) = mQueueFrames.add(
        frame
    )

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
    ) {

    }

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
        val inp = codec.getInputBuffer(
            index
        )

        if (inp == null) {
            Log.d(TAG, "processInputBuffer: NULL")
            return
        }

        inp.clear()
        var mSizeFrame = 0

        if (mQueueFrames.isNotEmpty()) {
            mQueueFrames.remove().packets.forEach {
                it?.apply {
                    val a = data.short(
                        MSUtilsAvc.OFFSET_PACKET_SIZE
                    )
                    inp.put(
                        data,
                        MSUtilsAvc.LEN_META,
                        a
                    )
                    mSizeFrame += a
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