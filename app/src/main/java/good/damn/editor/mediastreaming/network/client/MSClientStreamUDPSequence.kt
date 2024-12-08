package good.damn.editor.mediastreaming.network.client

import android.util.Log
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

    companion object {
        private const val TAG = "MSClientStreamUDPSequen"
    }

    private var mPosition = 0

    private val mBuffer = ByteArray(
        2048
    )

    private val mPacket = DatagramPacket(
        mBuffer,
        mBuffer.size
    )

    override fun hasQueueData() {
        if (mPosition < mBuffer.size) {
            mBuffer[mPosition] = mQueue.remove()
            mPosition++
            return
        }

        mPacket.address = host
        mPacket.port = port

        mPosition = 0

        try {
            mSocket.send(
                mPacket
            )
        } catch (e: Exception) {
            Log.d(TAG, "hasQueueData: ${e.message}")
        }
    }
}