package good.damn.media.streaming.network.client.tcp

import android.util.Log
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.Socket

class MSClientTCP
: Closeable {

    companion object {
        const val TIMEOUT_MS = 12000
        private const val TAG = "MSClientTCP"
    }

    private var mConnectedSocket: Socket? = null

    fun connect(
        host: InetSocketAddress
    ) = Socket().run {
        soTimeout = TIMEOUT_MS
        mConnectedSocket = this
        try {
            connect(
                host,
                TIMEOUT_MS
            )
        } catch (
            e: Exception
        ) {
            Log.d(TAG, "connect: ${e.message}")
            return@run null
        }

        return@run Pair(
            getInputStream(),
            getOutputStream()
        )
    }

    override fun close() {
        try {
            mConnectedSocket?.close()
        } catch (ignored: Exception) { }
    }

}