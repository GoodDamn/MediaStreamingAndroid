package good.damn.media.streaming.network.server.listeners

interface MSListenerServerOnReceiveSamples {
    fun onReceiveSamples(
        samples: ByteArray,
        offset: Int,
        len: Int
    )
}