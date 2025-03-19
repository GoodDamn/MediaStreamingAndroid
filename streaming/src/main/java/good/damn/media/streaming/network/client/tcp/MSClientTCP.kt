package good.damn.media.streaming.network.client.tcp

import good.damn.media.streaming.network.client.tcp.listeners.MSListenerOnConnectClientTCP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Closeable
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress

class MSClientTCP(
    private val scope: CoroutineScope
): Closeable {

    companion object {
        const val TIMEOUT = 3000
    }

    var onConnect: MSListenerOnConnectClientTCP? = null

    private val mSocket = Socket()

    fun connect(
        host: InetSocketAddress
    ) = scope.launch {
        try {
            mSocket.connect(
                host,
                TIMEOUT
            )
        } catch (e: Exception) {
            return@launch
        }

        onConnect?.onConnect(
            mSocket.getInputStream(),
            mSocket.getOutputStream()
        )
    }

    override fun close() {
        try {
            mSocket.close()
        } catch (ignored: Exception) { }
    }

}