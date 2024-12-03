package good.damn.editor.mediastreaming.network.server.listeners

interface MSListenerOnReceiveData {
    fun onReceiveData(
        data: ByteArray
    )
}