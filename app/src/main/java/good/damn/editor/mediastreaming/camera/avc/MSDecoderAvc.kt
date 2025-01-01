package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
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
import java.util.Arrays

class MSDecoderAvc
: MSCoder(),
MSStateable, MSListenerOnCombinePacket {

    companion object {
        private const val TAG = "MSDecoderAvc"
    }

    private val mStream = ByteArrayOutputStream()

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    private val mPacketCombiner = MSPacketCombiner()
    
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

        CoroutineScope(
            Dispatchers.IO
        ).launch {
            while (!isUninitialized) {
                processBuffer()
            }
        }
    }

    override fun onCombinePacket(
        packetId: Int,
        frame: MSPacketFrame
    ) {
        frame.chunks.forEach {
            it?.apply {
                mStream.write(
                    data
                )
            }
        }
        Log.d(TAG, "onCombinePacket: ${frame.chunks.size}")
    }

    private inline fun processBuffer() {
        val inputBufferId = mCoder.dequeueInputBuffer(
            0
        )
        if (inputBufferId >= 0) {

            val inp = mCoder.getInputBuffer(
                inputBufferId
            ) ?: return

            inp.clear()

            val buffer = mStream.toByteArray()
            mStream.reset()

            Log.d(TAG, "processBuffer: $inputBufferId")

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
                0,
                0
            )
        }

        val outId = mCoder.dequeueOutputBuffer(
            MediaCodec.BufferInfo(),
            0
        )

        if (outId >= 0) {
            mCoder.getOutputBuffer(
                outId
            )

            mCoder.releaseOutputBuffer(
                outId,
                true
            )
        }

        Log.d(TAG, "processBuffer: $outId $inputBufferId")


    }

}