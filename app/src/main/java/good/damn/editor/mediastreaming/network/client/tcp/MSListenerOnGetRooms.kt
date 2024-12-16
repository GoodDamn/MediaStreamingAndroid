package good.damn.editor.mediastreaming.network.client.tcp

import good.damn.editor.mediastreaming.network.server.room.MSRoom

interface MSListenerOnGetRooms {
    suspend fun onGetRooms(
        rooms: Array<MSModelRoomClient>
    )
}