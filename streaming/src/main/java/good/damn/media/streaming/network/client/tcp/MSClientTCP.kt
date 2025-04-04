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

class MSClientTCP
: Closeable {

    companion object {
        const val TIMEOUT = 3000
    }

    private val mSocket = Socket().apply {
        soTimeout = TIMEOUT
    }

    fun connect(
        host: InetSocketAddress
    ) = mSocket.run {
        try {
            connect(
                host,
                TIMEOUT
            )
        } catch (e: Exception) {
            return@run null
        }

        return@run Pair(
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