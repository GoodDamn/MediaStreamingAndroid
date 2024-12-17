package good.damn.editor.mediastreaming.network.server.accepters

import good.damn.editor.mediastreaming.extensions.readU
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnAcceptClient
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.room.MSRoomUser
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random

class MSAccepterConnectRoom(
    private val mRooms: MSRooms
): MSListenerOnAcceptClient {

    override fun onAcceptClient(
        socket: Socket,
        scope: CoroutineScope
    ) {
        val inp = socket.getInputStream()
        val out = socket.getOutputStream()

        if (inp.read() != 0xA) {
            inp.close()
            out.close()
            socket.close()
            return
        }

        mRooms.getRoomById(
            inp.readU()
        )?.apply {
            val newUserId = Random.nextInt(255)

            out.write(
                newUserId
            )

            users.add(
                MSRoomUser(
                    newUserId,
                    socket.inetAddress,
                    7777
                )
            )

            out.write(
                users.size
            )

            users.forEach {
                out.write(
                    it.id
                )
            }

            scope.launch {
                val lastId = users.lastOrNull()
                    ?.id
                    ?: return@launch

                users.forEach {
                    if (lastId == it.id) {
                        return@launch
                    }

                    val socket = Socket()

                    socket.connect(
                        InetSocketAddress(
                            it.host,
                            7780
                        ),
                        5000
                    )

                    socket.soTimeout = 5000

                    socket.getOutputStream().apply {
                        write(0xbb)
                        write(newUserId)
                    }

                    // Lock for response
                    try {
                        socket.getInputStream().read()
                    } catch (e: Exception) { }

                    socket.close()
                }
            }
        }
    }
}