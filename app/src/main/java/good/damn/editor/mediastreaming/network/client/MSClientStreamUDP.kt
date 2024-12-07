package good.damn.editor.mediastreaming.network.client

import good.damn.editor.mediastreaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

abstract class MSClientStreamUDP<DATA>(
    val port: Int,
    val scope: CoroutineScope
): MSStateable {

    companion object {
        private val TAG = MSClientStreamUDP::class.simpleName
    }

    var host = InetAddress.getByName(
        "0.0.0.0"
    )

    var isStreamRunning = false
        protected set

    protected var mSocket = DatagramSocket()

    protected val mQueue = ConcurrentLinkedQueue<DATA>()

    open fun sendToStream(
        data: DATA
    ) {
        if (isStreamRunning) {
            mQueue.add(
                data
            )
        }
    }

    override fun start() {
        scope.launch {
            isStreamRunning = true

            while (
                isStreamRunning
            ) {
                if (mQueue.isEmpty()) {
                    continue
                }

                hasQueueData()
            }

            mQueue.clear()
        }
    }

    override fun stop() {
        isStreamRunning = false
    }

    override fun release() {
        isStreamRunning = false
        mSocket.apply {
            disconnect()
            close()
        }
    }

    abstract fun hasQueueData()
}