package good.damn.media.streaming.network.server.listeners

import good.damn.media.streaming.models.handshake.MSMHandshakeAccept

interface MSListenerOnHandshakeSettings {
    suspend fun onHandshakeSettings(
        result: MSMHandshakeAccept
    )
}