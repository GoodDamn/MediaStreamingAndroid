package good.damn.media.streaming.network.client.tcp

import android.util.Log
import good.damn.media.streaming.extensions.readString
import good.damn.media.streaming.extensions.readU
import good.damn.media.streaming.network.client.tcp.listeners.MSListenerOnGetRooms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket

class MSClientGuildTCP(
    private val scope: CoroutineScope
): MSClientBaseTCP() {
    
    companion object {
        private val TAG = MSClientGuildTCP::class
            .simpleName
    }
    
    var onGetRooms: MSListenerOnGetRooms? = null

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
                val countRooms = readU()
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