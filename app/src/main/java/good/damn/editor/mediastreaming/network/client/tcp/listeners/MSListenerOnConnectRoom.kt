package good.damn.editor.mediastreaming.network.client.tcp.listeners

interface MSListenerOnConnectRoom {
    suspend fun onConnectRoom(
        userId: Int,
        users: Array<Int>?
    )
}