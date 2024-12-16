package good.damn.editor.mediastreaming.network.client.tcp

import good.damn.editor.mediastreaming.extensions.readU
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnConnectRoom
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnError
import good.damn.editor.mediastreaming.network.server.room.MSRoomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket

class MSClientConnectRoomTCP(
    private val scope: CoroutineScope
): MSClientBaseTCP() {

    var onConnectRoom: MSListenerOnConnectRoom? = null
    var onError: MSListenerOnError? = null

    fun connectToRoomAsync(
        roomId: Int
    ) = scope.launch {
        mSocket = Socket().apply {
            connect(
                host,
                5000
            )

            soTimeout = 5000

            getOutputStream().apply {
                write(0xA)
                write(roomId)
                flush()
            }

            try {
                getInputStream().apply {
                    val userRoomId = readU()
                    val users = Array(
                        readU()
                    ) {
                        readU() // guest user id
                    }

                    withContext(
                        Dispatchers.Main
                    ) {
                        onConnectRoom?.onConnectRoom(
                            userRoomId,
                            users
                        )
                    }

                    close()
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: ""
                withContext(
                    Dispatchers.Main
                ) {
                    onError?.onError(
                        msg
                    )
                }
            }

            close()
        }

    }

}