package good.damn.media.streaming.network.server.listeners

import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.network.client.tcp.MSNetworkDecoderSettings
import java.net.InetAddress

interface MSListenerOnHandshakeSettings {
    suspend fun onHandshakeSettings(
        settings: MSTypeDecoderSettings,
        fromIp: InetAddress
    )
}