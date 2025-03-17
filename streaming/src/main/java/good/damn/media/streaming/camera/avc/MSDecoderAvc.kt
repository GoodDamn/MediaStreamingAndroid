package good.damn.media.streaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.camera.avc.cache.MSListenerOnGetOrderedFrame
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.integer
import good.damn.media.streaming.extensions.short
import good.damn.media.streaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentLinkedQueue

class MSDecoderAvc
: MSCoder(),
MSStateable {

    companion object {
        private const val TAG = "MSDecoderAvc"
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    private val mQueueFrame = ConcurrentLinkedQueue<
        MSFrame
    >()

    var isConfigured = false
        private set

    var isRender = true

    override fun stop() {
        isConfigured = false
        super.stop()
    }

    override fun release() {
        isConfigured = false
        super.release()
    }

    override fun start() {
        if (!isConfigured) {
            return
        }
        super.start()
    }

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) = mCoder.run {
        isConfigured = true
        setCallback(
            this@MSDecoderAvc
        )
        configure(
            format,
            decodeSurface,
            null,
            0
        )
    }

    fun addOrderedFrame(
        frame: MSFrame
    ) {
        mQueueFrame.add(
            frame
        )
    }

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        try {
            val inp = codec.getInputBuffer(
                index
            )
            if (inp == null) {
                Log.d(TAG, "onInputBufferAvailable: NULL")
                return
            }

            var mSizeFrame = 0
            if (mQueueFrame.isNotEmpty()) {
                inp.clear()
                mQueueFrame.remove().packets.forEach {
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
        } catch (e: Exception) {
            Log.d(TAG, "onInputBufferAvailable: EXCEPTION: ${e.localizedMessage}")
        }
    }

    override fun onError(
        codec: MediaCodec,
        e: MediaCodec.CodecException
    ) {
        Log.d(TAG, "onError: ")
    }

    override fun onOutputFormatChanged(
        codec: MediaCodec, 
        format: MediaFormat
    ) {
        Log.d(TAG, "onOutputFormatChanged: ")
    }
    
    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        Log.d(TAG, "onOutputBufferAvailable: INFO: ${info.presentationTimeUs} ${info.offset} ${info.size}")

        try {
            codec.getOutputBuffer(
                index
            )

            codec.releaseOutputBuffer(
                index,
                isRender
            )
        } catch (_: Exception) {}
    }

}