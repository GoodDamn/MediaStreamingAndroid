package good.damn.media.streaming.network.server.room

import java.net.InetAddress

data class MSRoomUser(
    val id: Int,
    val host: InetAddress,
    val port: Int
)