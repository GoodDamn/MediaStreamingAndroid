package good.damn.editor.mediastreaming.network.server

import android.net.rtp.RtpStream
import android.util.Log
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import kotlinx.coroutines.delay
import java.net.DatagramPacket
import java.net.DatagramSocket

class MSReceiverAudioRoom(
    private val rooms: MSRooms
): MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverAudioRoom"
    }

    private val mSocket = DatagramSocket().apply {
        soTimeout = 100
        reuseAddress = true
        receiveBufferSize = 1
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val roomId = data[0].toInt() and 0xff
        val userId = data[1].toInt() and 0xff
        
        rooms.getRoomById(
            roomId
        )?.users?.apply {
            for (it in this) {
                if (userId == it.id) {
                    continue
                }

                try {
                    mSocket.send(
                        DatagramPacket(
                            data,
                            0,
                            data.size,
                            it.host,
                            it.port
                        )
                    )
                } catch (e: Exception) {
                    Log.d(TAG, "onReceiveData: ${e.localizedMessage}")
                }
            }
        }
    }

}