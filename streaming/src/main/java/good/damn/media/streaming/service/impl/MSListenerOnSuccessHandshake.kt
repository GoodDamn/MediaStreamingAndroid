package good.damn.media.streaming.service.impl

import good.damn.media.streaming.models.handshake.MSMHandshakeResult

interface MSListenerOnSuccessHandshake {
    suspend fun onSuccessHandshake(
        result: MSMHandshakeResult?
    )
}