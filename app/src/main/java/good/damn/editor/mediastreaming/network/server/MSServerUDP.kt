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

    private val mPacket = DatagramPacket(
        mBuffer,
        mBuffer.size
    )

    override fun start() {
        isRunning = true

        if (mSocket.isClosed) {
            mSocket = DatagramSocket(
                port
            ).apply {
                reuseAddress = true
                receiveBufferSize = 1
                sendBufferSize = 1
            }
        }

        scope.launch {
            while (
                isRunning
            ) { listen() }
        }
    }

    override fun stop() {
        isRunning = false
        mSocket.close()
    }

    override fun release() {
        mSocket.close()
    }

    private suspend inline fun listen() {
        try {
            mSocket.receive(
                mPacket
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