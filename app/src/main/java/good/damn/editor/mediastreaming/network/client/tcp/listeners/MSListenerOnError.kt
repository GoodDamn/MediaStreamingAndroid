package good.damn.editor.mediastreaming.network.client.tcp.listeners

interface MSListenerOnError {
    suspend fun onError(
        msg: String
    )
}