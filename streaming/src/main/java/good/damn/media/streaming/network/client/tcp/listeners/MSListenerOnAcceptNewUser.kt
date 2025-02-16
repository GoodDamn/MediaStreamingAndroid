package good.damn.media.streaming.network.client.tcp.listeners

interface MSListenerOnAcceptNewUser {
    fun onAcceptNewUser(
        userId: Int
    )
}