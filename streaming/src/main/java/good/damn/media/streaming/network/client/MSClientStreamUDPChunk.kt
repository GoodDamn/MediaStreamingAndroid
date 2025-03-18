package good.damn.media.streaming.network.client

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.log

class MSClientStreamUDPChunk(
    port: Int,
    scope: CoroutineScope
): MSClientStreamUDP<ByteArray>(
    port,
    scope
) {

    companion object {
        private val TAG = MSClientStreamUDPChunk::class
            .simpleName
    }

    private val mPacket = DatagramPacket(
        ByteArray(0),
        0,
        0,
        host,
        port
    )

    override fun sendToStream(
        data: ByteArray
    ) {
        if (!isStreamRunning) {
            return
        }

        mPacket.address = host

        mPacket.setData(
            data,
            0,
            data.size
        )

        mSocket.send(
            mPacket
        )
    }

    override fun start() {
        isStreamRunning = true
    }

    override fun hasQueueData() = Unit
}