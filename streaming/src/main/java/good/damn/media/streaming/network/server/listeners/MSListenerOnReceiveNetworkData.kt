package good.damn.media.streaming.network.server.listeners

import java.net.InetAddress

interface MSListenerOnReceiveNetworkData {
    suspend fun onReceiveNetworkData(
        data: ByteArray,
        src: InetAddress
    )
}