package good.damn.editor.mediastreaming.network.server

import android.util.Log
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket

class MSReceiverCameraFrameRoom(
    private val rooms: MSRooms
): MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverCameraFrameRo"
    }

    private val mShareSocket = DatagramSocket().apply {
        soTimeout = 50
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val userRoomId = data[3].toInt() and 0xff
        val roomId = data[4].toInt() and 0xff

        val users = rooms.getRoomById(
            roomId
        )?.users ?: return

        for (it in users) {
            Log.d(TAG, "onReceiveData: USER: ${it.id} $userRoomId")
            /*if (userRoomId == it.id) {
                continue
            }*/

            try {
                mShareSocket.send(
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