package good.damn.editor.mediastreaming.network.server.guild

import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnAcceptClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.ServerSocket

class MSServerTCP(
    private val port: Int,
    private val scope: CoroutineScope,
    private val accepter: MSListenerOnAcceptClient
): MSStateable {

    private var mSocket: ServerSocket? = null

    var isRunning = false
        private set

    override fun start() {
        scope.launch {
            mSocket = ServerSocket(
                port
            ).apply {
                soTimeout = 3000
                listen(this)
            }
        }
    }

    override fun stop() {
        release()
    }

    override fun release() {
        mSocket?.close()
        mSocket = null
    }

    private inline fun listen(
        socket: ServerSocket
    ) {
        val user = socket.accept()
        user.soTimeout = 4000

        accepter.onAcceptClient(
            user.inetAddress,
            user.getInputStream(),
            user.getOutputStream()
        )

        user.close()
    }
}