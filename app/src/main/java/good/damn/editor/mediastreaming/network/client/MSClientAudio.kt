package good.damn.editor.mediastreaming.network.client

import good.damn.editor.mediastreaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

class MSClientAudio(
    private val scope: CoroutineScope
): MSStateable {

    companion object {
        private val TAG = MSClientAudio::class.simpleName
    }

    var host = InetAddress.getByName(
        "0.0.0.0"
    )

    var isStreamRunning = false
        private set

    private var mSocket = DatagramSocket()

    private val mQueue = ConcurrentLinkedQueue<
        ByteArray
    >()

    fun sendToStream(
        data: ByteArray
    ) {
        if (isStreamRunning) {
            mQueue.add(
                data
            )
        }
    }

    override fun start() = scope.launch {
        isStreamRunning = true

        while (
            isStreamRunning
        ) {
            if (mQueue.isEmpty()) {
                continue
            }

            val data = mQueue.remove()

            val packet = DatagramPacket(
                data,
                0,
                data.size,
                host,
                5555
            )

            mSocket.send(
                packet
            )
        }

        mQueue.clear()
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

}