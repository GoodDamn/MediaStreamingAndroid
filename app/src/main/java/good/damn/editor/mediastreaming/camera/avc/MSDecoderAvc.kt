package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.camera.avc.cache.MSFrame
import good.damn.editor.mediastreaming.camera.avc.cache.MSListenerOnGetOrderedFrame
import good.damn.editor.mediastreaming.camera.avc.cache.MSPacketBufferizer
import good.damn.editor.mediastreaming.extensions.integer
import good.damn.editor.mediastreaming.extensions.short
import good.damn.editor.mediastreaming.network.MSStateable
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
        private const val TIMEOUT_USAGE_MS = 10_000L
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    private val mPacketBufferizer = MSPacketBufferizer().apply {
        onGetOrderedFrame = this@MSDecoderAvc
    }

    private val mQueueFrame = ConcurrentLinkedQueue<
        MSFrame
    >()

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

    override fun start() {
        super.start()
        CoroutineScope(
            Dispatchers.IO
        ).launch {
            while (isRunning) {
                mPacketBufferizer.orderPacket()
            }
        }
    }

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) = mCoder.run {
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

        codec.getOutputBuffer(
            index
        )

        codec.releaseOutputBuffer(
            index,
            true
        )
    }

}