package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.camera.avc.cache.MSListenerOnGetOrderedPacket
import good.damn.editor.mediastreaming.camera.avc.cache.MSPacket
import good.damn.editor.mediastreaming.camera.avc.cache.MSPacketBufferizer
import good.damn.editor.mediastreaming.extensions.integer
import good.damn.editor.mediastreaming.extensions.short
import good.damn.editor.mediastreaming.network.MSStateable
import java.util.concurrent.ConcurrentLinkedQueue

class MSDecoderAvc
: MSCoder(),
MSStateable,
MSListenerOnGetOrderedPacket {

    companion object {
        private const val TAG = "MSDecoderAvc"
        private const val TIMEOUT_USAGE_MS = 10_000L
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    private val mPacketBufferizer = MSPacketBufferizer()

    private val mQueueData = ConcurrentLinkedQueue<ByteArray>()

    fun writeData(
        data: ByteArray
    ) {
        /*mPacketBufferizer.write(
            data.integer(
                MSUtilsAvc.OFFSET_PACKET_ID
            ),
            data,
            this@MSDecoderAvc
        )*/
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

    override fun onGetOrderedPacket(
        frame: MSPacket
    ) {
        Log.d(TAG, "onGetOrderedPacket: $frame")
        mQueueData.add(
            frame.data
        )
    }

    private var mData = ByteArray(0)
    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        val inp = codec.getInputBuffer(
            index
        ) ?: return

        inp.clear()

        if (mQueueData.isNotEmpty()) {
            mData = mQueueData.remove()
            inp.put(
                mData,
                MSUtilsAvc.LEN_META,
                mData.size - MSUtilsAvc.LEN_META
            )
        }

        Log.d(TAG, "onInputBufferAvailable: ")
        codec.queueInputBuffer(
            index,
            0,
            mData.size - MSUtilsAvc.LEN_META,
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