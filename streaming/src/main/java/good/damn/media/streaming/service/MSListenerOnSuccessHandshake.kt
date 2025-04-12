package good.damn.media.streaming.service

interface MSListenerOnSuccessHandshake {
    suspend fun onSuccessHandshake(
        result: MSMHandshakeResult?
    )
}