package good.damn.editor.mediastreaming.network.client.tcp.listeners

interface MSListenerOnAcceptNewUser {
    fun onAcceptNewUser(
        userId: Int
    )
}