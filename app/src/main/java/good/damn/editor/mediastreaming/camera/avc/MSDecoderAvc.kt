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
import java.io.ByteArrayOutputStream

class MSDecoderAvc
: MSCoder(),
MSStateable,
MSListenerOnGetOrderedPacket {

    companion object {
        private const val TAG = "MSDecoderAvc"
        private const val TIMEOUT_USAGE_MS = 1_000_000L
    }

    private val mStream = ByteArrayOutputStream()

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    private val mPacketCombiner =
        MSPacketBufferizer()

    private val mBufferInfo = MediaCodec.BufferInfo()
    
    fun writeData(
        data: ByteArray
    ) {
        val copied = ByteArray(
            data.short(
                MSUtilsAvc.OFFSET_PACKET_SIZE
            )
        )

        System.arraycopy(
            data,
            MSUtilsAvc.LEN_META,
            copied,
            0,
            copied.size
        )

        mPacketCombiner.write(
            data.integer(
                MSUtilsAvc.OFFSET_PACKET_ID
            ),
            copied,
            this@MSDecoderAvc
        )
    }

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) = mCoder.run {
        configure(
            format,
            decodeSurface,
            null,
            0
        )
    }

    override fun start() {
        super.start()

        /*CoroutineScope(
            Dispatchers.IO
        ).launch {
            while (true) {
                if (!isRunning) {
                    mCoder.signalEndOfInputStream()
                    break
                }

                processBuffer()
            }
        }*/
    }

    override fun onGetOrderedPacket(
        frame: MSPacket
    ) {
        Log.d(TAG, "onGetOrderedPacket: $frame")
        //processBuffer()
    }

    private var mShitTime = 0L

    private inline fun processBuffer() {
        val inputBufferId = mCoder.dequeueInputBuffer(
             TIMEOUT_USAGE_MS
        )

        if (inputBufferId >= 0) {
            val inp = mCoder.inputBuffers[
                inputBufferId
            ]

            inp.clear()

            val buffer = mStream.toByteArray()
            mStream.reset()

            if (buffer.isNotEmpty()) {
                Log.d(TAG, "processBuffer: BUFFER_SIZE: ${buffer.size}")
            }

            inp.put(
                buffer
            )

            mCoder.queueInputBuffer(
                inputBufferId,
                0,
                buffer.size,
                TIMEOUT_USAGE_MS,
                0
            )
        }

        val outId = mCoder.dequeueOutputBuffer(
            mBufferInfo,
            TIMEOUT_USAGE_MS
        )

        Log.d(TAG, "processBuffer: $outId $inputBufferId ${mBufferInfo.size}")
        if (outId >= 0) {
            mCoder.releaseOutputBuffer(
                outId,
                true
            )
        }

        mShitTime += 66
    }

}