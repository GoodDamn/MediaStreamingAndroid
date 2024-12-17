package good.damn.editor.mediastreaming.network.server.accepters

import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnAcceptClient
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.Charset

class MSAccepterGetRoomList(
    private val mRooms: MSRooms
): MSListenerOnAcceptClient {

    override fun onAcceptClient(
        socket: Socket,
        scope: CoroutineScope
    ) {
        val inp = socket.getInputStream()
        val out = socket.getOutputStream()

        if (inp.read() != 0xf) {
            inp.close()
            out.close()
            return
        }

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