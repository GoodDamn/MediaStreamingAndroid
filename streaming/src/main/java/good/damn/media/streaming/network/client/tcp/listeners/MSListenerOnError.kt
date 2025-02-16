package good.damn.media.streaming.network.client.tcp.listeners

interface MSListenerOnError {
    suspend fun onError(
        msg: String
    )
}