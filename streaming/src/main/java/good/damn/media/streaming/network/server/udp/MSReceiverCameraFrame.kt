package good.damn.media.streaming.network.server.udp

import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.writeDefault
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveNetworkData
import java.net.InetAddress

class MSReceiverCameraFrame
: MSListenerOnReceiveNetworkData {

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    var bufferizer: MSPacketBufferizer? = null

    override suspend fun onReceiveNetworkData(
        data: ByteArray,
        src: InetAddress
    ) {
        bufferizer?.writeDefault(
            data
        )
    }
}