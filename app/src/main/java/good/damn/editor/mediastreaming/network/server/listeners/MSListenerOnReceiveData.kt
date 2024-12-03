package good.damn.editor.mediastreaming.network.server.listeners

interface MSListenerOnReceiveData {
    suspend fun onReceiveData(
        data: ByteArray
    )
}