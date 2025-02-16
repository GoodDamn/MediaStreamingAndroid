package good.damn.media.streaming.network.server.listeners

interface MSListenerOnReceiveData {
    suspend fun onReceiveData(
        data: ByteArray
    )
}