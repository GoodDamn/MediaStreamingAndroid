package good.damn.editor.mediastreaming.network.client

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
): MSClientStreamUDP<MSModelChunkUDP>(
    port,
    scope
) {

    companion object {
        private val TAG = MSClientStreamUDPChunk::class
            .simpleName
    }

    override fun sendToStream(
        data: MSModelChunkUDP
    ) {
        if (!isStreamRunning) {
            return
        }

        mSocket.send(
            DatagramPacket(
                data.data,
                data.offset,
                data.len,
                host,
                port
            )
        )
    }

    override fun start() {
        isStreamRunning = true
    }

    override fun hasQueueData() = Unit
}