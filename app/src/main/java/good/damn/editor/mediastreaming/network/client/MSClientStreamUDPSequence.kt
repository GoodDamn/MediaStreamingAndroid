package good.damn.editor.mediastreaming.network.client

import kotlinx.coroutines.CoroutineScope
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentLinkedQueue

class MSClientStreamUDPSequence(
    port: Int,
    scope: CoroutineScope
): MSClientStreamUDP<Byte>(
    port,
    scope
) {

    private var mPosition = 0

    private val mBuffer = ByteArray(
        60000
    )

    override fun hasQueueData() {
        if (mPosition < mBuffer.size) {
            mBuffer[mPosition] = mQueue.remove()
            mPosition++
            return
        }

        mPosition = 0

        mSocket.send(
            DatagramPacket(
                mBuffer,
                0,
                mBuffer.size,
                host,
                port
            )
        )
    }
}