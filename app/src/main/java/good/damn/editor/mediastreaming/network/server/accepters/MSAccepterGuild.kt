package good.damn.editor.mediastreaming.network.server.accepters

import good.damn.editor.mediastreaming.extensions.readU
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnAcceptClient
import good.damn.editor.mediastreaming.network.server.room.MSRoomUser
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset
import kotlin.random.Random

class MSAccepterGuild(
    private val mRooms: MSRooms
): MSListenerOnAcceptClient {

    override fun onAcceptClient(
        socket: Socket,
        scope: CoroutineScope
    ) {
        val inp = socket.getInputStream()
        val out = socket.getOutputStream()

        when (inp.read()) {

            0xf -> sendRoomsList(
                inp,
                out
            )

            0xA -> createNewUserSendUsers(
                inp,
                out,
                scope,
                socket.inetAddress
            )

            else -> {
                inp.close()
                out.close()
                socket.close()
            }
        }
    }

    private inline fun createNewUserSendUsers(
        inp: InputStream,
        out: OutputStream,
        scope: CoroutineScope,
        fromAddress: InetAddress
    ) {
        mRooms.getRoomById(
            inp.readU()
        )?.apply {
            val newUserId = Random.nextInt(255)

            out.write(
                newUserId
            )

            out.write(
                users.size
            )

            users.forEach {
                out.write(
                    it.id
                )
            }

            users.add(
                MSRoomUser(
                    newUserId,
                    fromAddress,
                    7777
                )
            )

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

    private inline fun sendRoomsList(
        inp: InputStream,
        out: OutputStream
    ) {
        out.write(
            mRooms.size
        )

        for (it in mRooms.entries) {
            out.write(
                it.key
            )

            val roomName = it.value.toString().toByteArray(
                Charset.forName(
                    "UTF-8"
                )
            )

            out.write(
                roomName.size
            )

            out.write(
                roomName
            )

            out.write(
                it.value.users.size
            )
        }
    }

}