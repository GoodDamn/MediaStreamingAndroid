package good.damn.editor.mediastreaming.network.client

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentLinkedQueue

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
        Log.d(TAG, "sendToStream: $data")
        if (!isStreamRunning) {
            return
        }

        mSocket.send(
            DatagramPacket(
                data.data,
                0,
                data.len,
                host,
                port
            )
        )
    }

    override fun start() = scope.launch {
        isStreamRunning = true
    }

    override fun hasQueueData() = Unit
}