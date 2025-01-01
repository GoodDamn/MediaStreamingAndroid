package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.camera.avc.cache.MSListenerOnCombinePacket
import good.damn.editor.mediastreaming.camera.avc.cache.MSPacketCombiner
import good.damn.editor.mediastreaming.camera.avc.cache.MSPacketFrame
import good.damn.editor.mediastreaming.extensions.integer
import good.damn.editor.mediastreaming.extensions.short
import good.damn.editor.mediastreaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MSDecoderAvc
: MSCoder(),
MSStateable, MSListenerOnCombinePacket {

    companion object {
        private const val TAG = "MSDecoderAvc"
        private const val TIMEOUT_USAGE_MS = 1_000_000L
    }

    private val mStream = ByteArrayOutputStream()

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    private val mPacketCombiner = MSPacketCombiner()

    private val mBufferInfo = MediaCodec.BufferInfo()
    
    fun writeData(
        data: ByteArray
    ) {
        val copied = ByteArray(
            data.short(8)
        )

        System.arraycopy(
            data,
            10,
            copied,
            0,
            copied.size
        )

        mPacketCombiner.write(
            data.integer(0),
            data.short(4).toShort(),
            data.short(6).toShort(),
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

    override fun onCombinePacket(
        packetId: Int,
        frame: MSPacketFrame
    ) {
        frame.chunks.forEachIndexed { index, msPacket ->
            msPacket?.apply {
                Log.d(TAG, "onCombinePacket: ID: $packetId; $index/${frame.chunks.size} ${data.size}")
                mStream.write(
                    data
                )
            }
        }

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