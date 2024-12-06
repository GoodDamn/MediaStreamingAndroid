package good.damn.editor.mediastreaming.network.client

import good.damn.editor.mediastreaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

class MSClientStreamUDP(
    private val port: Int,
    private val scope: CoroutineScope
): MSStateable {

    companion object {
        private val TAG = MSClientStreamUDP::class.simpleName
    }

    var host = InetAddress.getByName(
        "0.0.0.0"
    )

    var isStreamRunning = false
        private set

    private val mBuffer = ByteArray(
        60000
    )

    private var mPosition = 0

    private var mSocket = DatagramSocket()

    private val mQueue = ConcurrentLinkedQueue<Byte>()

    fun sendToStream(
        data: Byte
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

            if (mPosition < mBuffer.size) {
                mBuffer[mPosition] = mQueue.remove()
                mPosition++
                continue
            }

            mPosition = 0

            val packet = DatagramPacket(
                mBuffer,
                0,
                mBuffer.size,
                host,
                port
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