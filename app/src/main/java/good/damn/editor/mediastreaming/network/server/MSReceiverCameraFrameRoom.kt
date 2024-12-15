package good.damn.editor.mediastreaming.network.server

import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket

class MSReceiverCameraFrameRoom(
    private val rooms: MSRooms
): MSListenerOnReceiveData {

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val roomId = data[0].toInt() and 0xff
        val userRoomId = data[1].toInt() and 0xff

        val users = rooms.getRoomById(
            roomId
        )?.users ?: return

        for (it in users) {
            if (userRoomId == it.id) {
                continue
            }

            DatagramSocket().apply {
                send(
                    DatagramPacket(
                        data,
                        0,
                        data.size,
                        it.host,
                        it.port
                    )
                )

                close()
            }
        }
    }

}