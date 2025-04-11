package good.damn.media.streaming.service

import good.damn.media.streaming.MSTypeDecoderSettings

interface MSListenerOnSuccessHandshake {
    suspend fun onSuccessHandshake(
        result: MSMHandshake
    )
}