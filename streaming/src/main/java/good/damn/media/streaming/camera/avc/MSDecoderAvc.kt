package good.damn.media.streaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.camera.avc.cache.MSListenerOnGetOrderedFrame
import good.damn.media.streaming.extensions.integer
import good.damn.media.streaming.extensions.short
import good.damn.media.streaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class MSDecoderAvc
: MSCoder(),
    MSStateable,
    MSListenerOnGetOrderedFrame {

    companion object {
        private const val TAG = "MSDecoderAvc"
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    private val mPacketBufferizer = good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer().apply {
        onGetOrderedFrame = this@MSDecoderAvc
    }

    private val mQueueFrame = ConcurrentLinkedQueue<
        MSFrame
    >()

    private var mSavedSurface: Surface? = null
    private var mSavedFormat: MediaFormat? = null

    var isConfigured = false
        private set

    fun writeData(
        data: ByteArray
    ) {
        mPacketBufferizer.write(
            data.integer(
                MSUtilsAvc.OFFSET_PACKET_FRAME_ID
            ),
            data.short(
                MSUtilsAvc.OFFSET_PACKET_ID
            ).toShort(),
            data.short(
                MSUtilsAvc.OFFSET_PACKET_COUNT
            ).toShort(),
            data
        )
    }


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
            mSavedFormat ?: return
            mSavedSurface ?: return

            configure(
                mSavedSurface!!,
                mSavedFormat!!
            )
        }
        super.start()
        CoroutineScope(
            Dispatchers.IO
        ).launch {
            while (isRunning) {
                mPacketBufferizer.orderPacket()
            }

            mPacketBufferizer.clear()
        }
    }

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) = mCoder.run {
        mSavedSurface = decodeSurface
        mSavedFormat = format

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

    override fun onGetOrderedFrame(
        frame: MSFrame
    ) {
        Log.d(TAG, "onGetOrderedFrame: $frame")
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
            ) ?: return

            inp.clear()

            var s = 0
            if (mQueueFrame.isNotEmpty()) {
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
                        s += a
                    }
                }

                Log.d(TAG, "onInputBufferAvailable: $s")
            }

            codec.queueInputBuffer(
                index,
                0,
                s,
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
                true
            )
        } catch (_: Exception) {}
    }

}