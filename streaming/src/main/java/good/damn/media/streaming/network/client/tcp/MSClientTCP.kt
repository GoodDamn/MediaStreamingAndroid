package good.damn.media.streaming.network.client.tcp

import java.io.Closeable
import java.net.InetSocketAddress
import java.net.Socket

class MSClientTCP
: Closeable {

    companion object {
        const val TIMEOUT_MS = 7000
    }

    private val mSocket = Socket().apply {
        soTimeout = TIMEOUT_MS
    }

    fun connect(
        host: InetSocketAddress
    ) = mSocket.run {
        try {
            connect(
                host,
                TIMEOUT_MS
            )
        } catch (
            e: Exception
        ) {
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