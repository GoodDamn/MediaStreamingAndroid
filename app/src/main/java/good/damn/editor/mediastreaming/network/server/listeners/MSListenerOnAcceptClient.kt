package good.damn.editor.mediastreaming.network.server.listeners

import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket

interface MSListenerOnAcceptClient {
    fun onAcceptClient(
        socket: Socket,
        scope: CoroutineScope
    )
}