package good.damn.media.streaming.network.server

import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import good.damn.media.streaming.camera.avc.MSDecoderAvc
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.camera.avc.cache.MSListenerOnGetOrderedFrame
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.writeDefault
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveData

class MSReceiverCameraFrame
: MSListenerOnReceiveData,
MSListenerOnGetOrderedFrame {

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    var bufferizer: MSPacketBufferizer? = null

    val isDecoding: Boolean
        get() = mDecoder.isRunning

    private val mDecoder = MSDecoderAvc()

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) {
        mDecoder.configure(
            decodeSurface,
            format
        )
    }

    fun startDecoding() {
        mDecoder.start()
    }

    fun stop() {
        mDecoder.stop()
    }

    fun release() {
        mDecoder.release()
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        bufferizer?.writeDefault(
            data
        )
    }

    override fun onGetOrderedFrame(
        frame: MSFrame
    ) {
        mDecoder.addOrderedFrame(
            frame
        )
    }
}