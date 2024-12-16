package good.damn.editor.mediastreaming.network.client.tcp

import android.util.Log
import good.damn.editor.mediastreaming.extensions.readString
import good.damn.editor.mediastreaming.extensions.readU
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.server.room.MSRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

class MSClientGuildTCP(
    private val scope: CoroutineScope
) {
    
    companion object {
        private val TAG = MSClientGuildTCP::class
            .simpleName
    }
    
    var onGetRooms: MSListenerOnGetRooms? = null

    var host: InetSocketAddress? = null

    private var mSocket: Socket? = null

    fun getRoomsAsync() = scope.launch {
        val host = host
            ?: return@launch

        mSocket = Socket().apply {
            connect(
                host,
                5000
            )


            getOutputStream().apply {
                write(0xf)
                flush()
            }

            getInputStream().apply {
                Log.d(TAG, "getRoomsAsync: ")
                val countRooms = readU()
                Log.d(TAG, "getRoomsAsync: countRooms $countRooms")
                val rooms = Array(
                    countRooms
                ) {
                    val id = readU()
                    val roomNameSize = readU()
                    val roomName = readString(
                        roomNameSize
                    )
                    val countUsers = readU()

                    MSModelRoomClient(
                        id,
                        roomName,
                        countUsers
                    )
                }
                Log.d(TAG, "getRoomsAsync: ROOMS $rooms")

                withContext(
                    Dispatchers.Main
                ) {
                    onGetRooms?.onGetRooms(
                        rooms
                    )
                }

                close()
            }

            close()

        }

    }
}