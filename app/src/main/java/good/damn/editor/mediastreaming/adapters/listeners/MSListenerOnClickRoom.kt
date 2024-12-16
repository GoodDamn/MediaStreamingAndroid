package good.damn.editor.mediastreaming.adapters.listeners

import good.damn.editor.mediastreaming.network.client.tcp.MSModelRoomClient

interface MSListenerOnClickRoom {
    fun onClickRoom(
        room: MSModelRoomClient
    )
}