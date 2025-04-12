package good.damn.media.streaming.network.server.udp

import good.damn.media.streaming.MSStreamConstantsPacket
import good.damn.media.streaming.extensions.integerBE
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveNetworkData
import java.net.InetAddress

class MSReceiverCameraFrame
: MSListenerOnReceiveNetworkData {

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    var users: Map<Int, MSIReceiverCameraFrameUser>? = null

    override suspend fun onReceiveNetworkData(
        data: ByteArray,
        src: InetAddress
    ) {
        users?.get(
            data.integerBE(
                MSStreamConstantsPacket.OFFSET_PACKET_SRC_ID
            )
        )?.receiveUserFrame(
            data
        )
    }
}