package good.damn.media.streaming.network.client.tcp

data class MSModelRoomClient(
    val id: Int,
    val roomName: String,
    val countUsers: Int
)