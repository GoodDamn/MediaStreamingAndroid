package good.damn.media.streaming.network.server.udp

import android.media.MediaFormat
import android.view.Surface
import good.damn.media.streaming.camera.avc.MSDecoderAvc
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.writeDefault
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveData

class MSReceiverCameraFrame
: MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    var bufferizer: MSPacketBufferizer? = null

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        bufferizer?.writeDefault(
            data
        )
    }
}