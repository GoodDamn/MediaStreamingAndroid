package good.damn.media.streaming.camera

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import good.damn.media.streaming.MSStreamConstantsPacket
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.extensions.short
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

class MSCameraCallbackDecoder(
    private val codecBuffer: MSCameraCodecBuffers
): MediaCodec.Callback() {

    companion object {
        private const val TAG = "MSCameraCallbackDecoder"
    }

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        codecBuffer.addAvailableBufferIndex(
            index
        )
    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        try {
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
    ) = Unit

    override fun onOutputFormatChanged(
        codec: MediaCodec,
        format: MediaFormat
    ) = Unit
}