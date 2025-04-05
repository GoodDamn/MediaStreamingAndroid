package good.damn.media.streaming.network.server.listeners

import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket

interface MSListenerOnAcceptClient {
    suspend fun onAcceptClient(
        socket: Socket
    )
}