package good.damn.editor.mediastreaming.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue

class MSClientAudio {

    companion object {
        private val TAG = MSClientAudio::class.simpleName
    }

    var host = InetAddress.getByName(
        "0.0.0.0"
    )

    var isStreamStopped = false
        private set

    private var mSocket = DatagramSocket()

    private val mScope = CoroutineScope(
        Dispatchers.IO
    )

    private val mQueue = ConcurrentLinkedQueue<ByteArray>()

    fun stream() = mScope.launch {
        while (
            !isStreamStopped
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

    fun sendToMediaServer(
        data: ByteArray
    ) {
        if (isStreamStopped) {
            return
        }

        mQueue.add(
            data
        )
    }

    fun stopStream() {
        isStreamStopped = true
    }

}