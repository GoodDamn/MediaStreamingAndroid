package good.damn.editor.mediastreaming.network.server

import android.util.Log
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket

open class MSServerUDP(
    port: Int,
    bufferSize: Int,
    private val scope: CoroutineScope,
    private val onReceiveData: MSListenerOnReceiveData
): MSStateable {

    companion object {
        private val TAG = MSServerUDP::class.simpleName
    }

    var isRunning = false
        private set

    private val mBuffer = ByteArray(
        bufferSize
    )

    private val mSocket = DatagramSocket(
        port
    )

    override fun start() = scope.launch {
        isRunning = true
        while (
            isRunning
        ) { listen() }
    }

    override fun stop() {
        isRunning = false
        mSocket.close()
    }

    override fun release() {
        mSocket.close()
    }

    private suspend inline fun listen() {
        mSocket.receive(
            DatagramPacket(
                mBuffer,
                mBuffer.size
            )
        )

        onReceiveData.onReceiveData(
            mBuffer
        )
    }

}