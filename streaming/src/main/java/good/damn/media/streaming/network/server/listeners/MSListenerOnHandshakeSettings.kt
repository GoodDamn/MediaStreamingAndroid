package good.damn.media.streaming.network.server.listeners

import good.damn.media.streaming.MSTypeDecoderSettings
import java.net.InetAddress

interface MSListenerOnHandshakeSettings {
    suspend fun onHandshakeSettings(
        settings: MSTypeDecoderSettings,
        fromIp: InetAddress
    )
}