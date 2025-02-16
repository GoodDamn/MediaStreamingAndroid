package good.damn.media.streaming.network.client.tcp.listeners

interface MSListenerOnConnectRoom {
    suspend fun onConnectRoom(
        userId: Int,
        users: Array<Int>?
    )
}