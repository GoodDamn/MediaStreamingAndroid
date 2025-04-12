package good.damn.media.streaming.network.server.listeners

import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.service.MSMHandshakeAccept
import java.net.InetAddress

interface MSListenerOnHandshakeSettings {
    suspend fun onHandshakeSettings(
        result: MSMHandshakeAccept
    )
}