package good.damn.media.streaming.network.server.tcp

import android.os.Handler
import android.util.Log
import good.damn.media.streaming.network.MSStateable
import good.damn.media.streaming.network.server.listeners.MSListenerOnAcceptClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.ServerSocket

class MSServerTCP(
    private val accepter: MSListenerOnAcceptClient,
    private val scope: CoroutineScope
) {

    companion object {
        private val TAG = MSServerTCP::class.simpleName
    }
    
    private var mSocket: ServerSocket? = null
    private var mJob: Job? = null

    var isRunning = false
        private set

    fun start(
        port: Int
    ) {
        isRunning = true

        mSocket = ServerSocket().apply {
            reuseAddress = true
            bind(InetSocketAddress(port))
        }

        mJob = scope.launch {
            mSocket?.apply {
                while (
                    isRunning
                ) { listen(this) }
            }
        }
    }

    fun stop() {
        release()
    }

    fun release() {
        isRunning = false
        mJob?.cancel()
        mJob = null
        mSocket?.close()
        mSocket = null
    }

    private suspend inline fun listen(
        socket: ServerSocket
    ) = try {
        Log.d(TAG, "listen: ")
        val user = socket.accept()
        Log.d(TAG, "listen: accept")
        user.soTimeout = 7000

        accepter.onAcceptClient(
            user
        )
    } catch (e: Exception) {
        Log.d(TAG, "listen: ${e.message}")
    }
}