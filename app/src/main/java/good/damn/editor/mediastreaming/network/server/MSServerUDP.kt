package good.damn.editor.mediastreaming.network.server

import android.provider.ContactsContract.Data
import android.system.ErrnoException
import android.util.Log
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket

open class MSServerUDP(
    private val port: Int,
    bufferSize: Int,
    private val scope: CoroutineScope,
    private val onReceiveData: MSListenerOnReceiveData
): MSStateable {

    companion object {
        private val TAG = MSServerUDP::class.simpleName
    }

    var isRunning = false
        private set

    private var mBuffer = ByteArray(
        bufferSize
    )

    private var mSocket = DatagramSocket(
        port
    ).apply {
        reuseAddress = true
        receiveBufferSize = 1
        sendBufferSize = 1
    }

    override fun start() {
        if (mSocket.isClosed) {
            mSocket = DatagramSocket(
                port
            ).apply {
                reuseAddress = true
                receiveBufferSize = 1
                sendBufferSize = 1
            }
        }

        isRunning = true
        scope.launch {
            Log.d(TAG, "start: isRunning: START:")
            while (
                isRunning
            ) { listen() }
            Log.d(TAG, "start: isRunning: END")
        }
    }


    override fun stop() {
        isRunning = false
    }

    override fun release() {
        isRunning = false
        mSocket.close()
    }

    private inline fun listen() {
        Log.d(TAG, "listen: BUFFER: $mBuffer")
        try {
            mSocket.receive(
                DatagramPacket(
                    mBuffer,
                    mBuffer.size
                )
            )
        } catch (e: Exception) {
            Log.d(TAG, "listen: ${e.localizedMessage}")
        }

        val saved = mBuffer
        scope.launch {
            onReceiveData.onReceiveData(
                saved
            )
        }

        mBuffer = ByteArray(
            mBuffer.size
        )
    }

}