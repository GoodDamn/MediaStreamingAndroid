package good.damn.editor.mediastreaming.network.client.tcp.listeners

import good.damn.editor.mediastreaming.network.client.tcp.MSModelRoomClient

interface MSListenerOnGetRooms {
    suspend fun onGetRooms(
        rooms: Array<MSModelRoomClient>
    )
}