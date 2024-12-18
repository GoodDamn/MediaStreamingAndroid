package good.damn.editor.mediastreaming.network.client

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import java.net.DatagramPacket

class MSClientStreamUDPAudio(
    port: Int,
    scope: CoroutineScope,
    bufferSize: Int
): MSClientStreamUDP<Byte>(
    port,
    scope
) {

    companion object {
        private const val TAG = "MSClientStreamUDPSequen"
    }

    var roomId: Byte = -1
    var userId: Byte = -1

    private var mPosition = 0

    private val mBuffer = ByteArray(
        bufferSize
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

        mBuffer[0] = roomId
        mBuffer[1] = userId

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