package good.damn.media.streaming.network.client.tcp.listeners

import good.damn.media.streaming.network.client.tcp.MSModelRoomClient


interface MSListenerOnGetRooms {
    suspend fun onGetRooms(
        rooms: Array<MSModelRoomClient>
    )
}