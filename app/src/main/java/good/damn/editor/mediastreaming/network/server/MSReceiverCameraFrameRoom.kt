package good.damn.editor.mediastreaming.network.server

import android.util.Log
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket

class MSReceiverCameraFrameRoom(
    private val rooms: MSRooms
): MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverCameraFrameRo"
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val roomId = data[4].toInt() and 0xff
        val userRoomId = data[3].toInt() and 0xff

        val users = rooms.getRoomById(
            roomId
        )?.users ?: return

        for (it in users) {
            /*if (userRoomId == it.id) {
                continue
            }*/

            try {
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
            } catch (e: Exception) {
                Log.d(TAG, "onReceiveData: ${e.localizedMessage}")
            }
        }
    }

}