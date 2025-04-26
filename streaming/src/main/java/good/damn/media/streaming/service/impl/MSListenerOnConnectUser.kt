package good.damn.media.streaming.service.impl

import good.damn.media.streaming.models.handshake.MSMHandshakeAccept

interface MSListenerOnConnectUser {
    fun onConnectUser(
        model: MSMHandshakeAccept
    )
}