package good.damn.editor.mediastreaming.network.server

import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import java.net.DatagramPacket
import java.net.DatagramSocket

class MSReceiverAudioRoom(
    private val rooms: MSRooms
): MSListenerOnReceiveData {

    private val mSocket = DatagramSocket().apply {
        soTimeout = 100
        reuseAddress = true
        receiveBufferSize = 1
        sendBufferSize = 1
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val roomId = data[0]
        val userId = data[1].toInt() and 0xff

        rooms.getRoomById(
            roomId.toInt() and 0xff
        )?.users?.forEach {
            mSocket.send(
                DatagramPacket(
                    data,
                    0,
                    data.size,
                    it.host,
                    it.port
                )
            )
        }
    }

}