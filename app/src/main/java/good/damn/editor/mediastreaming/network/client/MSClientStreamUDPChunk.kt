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

    private val mPacket = DatagramPacket(
        ByteArray(1),
        0,
        1
    )

    override fun sendToStream(
        data: MSModelChunkUDP
    ) {
        mPacket.data = data.data
        mPacket.length = data.len
        mPacket.address = host
        mPacket.port = port
        mPacket.setData(
            data.data,
            0,
            data.len
        )
        Log.d(TAG, "sendToStream: $data")

        if (!isStreamRunning) {
            return
        }
        mSocket.send(
            mPacket
        )
    }

    override fun start() {
        isStreamRunning = true
    }

    override fun hasQueueData() = Unit
}