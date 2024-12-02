package good.damn.editor.mediastreaming.network.server.listeners

interface MSListenerServerOnReceiveSamples {
    fun onReceiveSamples(
        samples: ByteArray,
        offset: Int,
        len: Int
    )
}