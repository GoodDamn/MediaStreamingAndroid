package good.damn.media.streaming.network.server

import android.provider.ContactsContract.Data
import android.system.ErrnoException
import android.util.Log
import good.damn.media.streaming.network.MSStateable
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveData
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

    private val mPacket = DatagramPacket(
        mBuffer,
        mBuffer.size
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

    private suspend inline fun listen() {
        try {
            mPacket.setData(
                mBuffer,
                0,
                mBuffer.size
            )
            mSocket.receive(
                mPacket
            )
        } catch (e: Exception) {
            Log.d(TAG, "listen: ${e.localizedMessage}")
        }

        val saved = mBuffer
        withContext(
            Dispatchers.IO
        ) {
            onReceiveData.onReceiveData(
                saved
            )
        }

        mBuffer = ByteArray(
            mBuffer.size
        )
    }

}