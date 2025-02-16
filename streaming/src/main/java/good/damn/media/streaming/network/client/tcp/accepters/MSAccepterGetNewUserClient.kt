package good.damn.media.streaming.network.client.tcp.accepters

import good.damn.media.streaming.extensions.readU
import good.damn.media.streaming.network.client.tcp.listeners.MSListenerOnAcceptNewUser
import good.damn.media.streaming.network.server.listeners.MSListenerOnAcceptClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket

class MSAccepterGetNewUserClient
: MSListenerOnAcceptClient {

    var onAcceptNewUser: MSListenerOnAcceptNewUser? = null

    override fun onAcceptClient(
        socket: Socket,
        scope: CoroutineScope
    ) {
        val inp = socket.getInputStream()
        val out = socket.getOutputStream()

        if (inp.read() != 0xbb) {
            inp.close()
            out.close()
            socket.close()
            return
        }

        val userId = inp.readU()
        onAcceptNewUser?.onAcceptNewUser(
            userId
        )

        out.write(0x1)
    }

}