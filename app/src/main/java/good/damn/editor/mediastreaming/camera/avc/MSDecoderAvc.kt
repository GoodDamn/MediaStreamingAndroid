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
import java.io.ByteArrayOutputStream
import java.util.Arrays

class MSDecoderAvc
: MSCoder(),
MSStateable, MSListenerOnCombinePacket {

    companion object {
        private const val TAG = "MSDecoderAvc"
    }

    private var mBuffer = ByteArray(0)

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
            packetId = data.integer(0),
            chunkId = data.short(4).toShort(),
            chunkCount = data.short(6).toShort(),
            copied,
            this@MSDecoderAvc
        )
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

    override fun onCombinePacket(
        packetId: Int,
        frame: MSPacketFrame
    ) {
        synchronized(
            mStream
        ) {
            frame.chunks.forEach {
                mStream.write(
                    it.value.data
                )
            }
            Log.d(TAG, "onCombinePacket: ${frame.chunks.size}")
        }
    }

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        Log.d(TAG, "onInputBufferAvailable: $index")

        if (isUninitialized) {
            return
        }

        try {
            val inp = codec.getInputBuffer(
                index
            ) ?: return

            inp.clear()

            synchronized(
                mStream
            ) {
                mBuffer = mStream.toByteArray()
                mStream.reset()
            }

            if (mBuffer.isNotEmpty()) {
                Log.d(TAG, "onInputBufferAvailable: BUFFER_SIZE: ${mBuffer.size}")
            }
            if (mBuffer.size > inp.capacity()) {
                return
            }

            inp.put(
                mBuffer,
                0,
                mBuffer.size
            )

            codec.queueInputBuffer(
                index,
                0,
                mBuffer.size,
                0,
                0
            )
        } catch (e: Exception) {

        }
    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        Log.d(TAG, "onOutputBufferAvailable: $index")

        if (isUninitialized) {
            return
        }

        try {
            codec.getOutputBuffer(
                index
            )

            codec.releaseOutputBuffer(
                index,
                true
            )
        } catch (e: Exception) {

        }
    }

    override fun onError(
        codec: MediaCodec,
        e: MediaCodec.CodecException
    ) {
        Log.d(TAG, "onError: ${e.localizedMessage}")
    }

    override fun onOutputFormatChanged(
        codec: MediaCodec,
        format: MediaFormat
    ) {
        Log.d(TAG, "onOutputFormatChanged: $format")
    }

}