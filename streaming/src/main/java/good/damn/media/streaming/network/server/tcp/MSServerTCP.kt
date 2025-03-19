package good.damn.media.streaming.network.server.tcp

import android.util.Log
import good.damn.media.streaming.network.MSStateable
import good.damn.media.streaming.network.server.listeners.MSListenerOnAcceptClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.ServerSocket

class MSServerTCP(
    private val port: Int,
    private val scope: CoroutineScope,
    private val accepter: MSListenerOnAcceptClient
): MSStateable {

    companion object {
        private val TAG = MSServerTCP::class.simpleName
    }
    
    private var mSocket: ServerSocket? = null

    var isRunning = false
        private set

    override fun start() {
        isRunning = true
        scope.launch {
            mSocket = ServerSocket(
                port
            ).apply {
                while (
                    isRunning
                ) { listen(this) }
            }
        }
    }

    override fun stop() {
        release()
    }

    override fun release() {
        isRunning = false
        mSocket?.close()
        mSocket = null
    }

    private inline fun listen(
        socket: ServerSocket
    ) {
        Log.d(TAG, "listen: ")
        val user = socket.accept()
        Log.d(TAG, "listen: accept")
        user.soTimeout = 4000

        accepter.onAcceptClient(
            user,
            scope
        )
    }
}