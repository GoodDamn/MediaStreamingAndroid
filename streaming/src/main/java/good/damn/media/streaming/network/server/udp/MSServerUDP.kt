package good.damn.media.streaming.network.server.udp

import android.util.Log
import good.damn.media.streaming.network.MSStateable
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveNetworkData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket

open class MSServerUDP(
    private val port: Int,
    bufferSize: Int,
    private val scope: CoroutineScope,
    private val onReceiveData: MSListenerOnReceiveNetworkData
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
    }

    private var mJob: Job? = null

    override fun start() {
        if (mSocket.isClosed) {
            mSocket = DatagramSocket(
                port
            ).apply {
                reuseAddress = true
            }
        }

        isRunning = true
        mJob = scope.launch {
            Log.d(TAG, "start: isRunning: START: $onReceiveData")
            while (
                isRunning
            ) { listen() }
            Log.d(TAG, "start: isRunning: END: $onReceiveData")
        }
    }

    override fun stop() {
        isRunning = false
        mJob?.cancel()
    }

    override fun release() {
        isRunning = false
        mJob?.cancel()
        try {
            // causes infinite loop
            //mSocket.disconnect()
            mSocket.close()
        } catch (ignored: Exception) {}
    }

    private suspend fun listen() {
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
        val address = mPacket.address
        val saved = mBuffer
        if (address != null) {
            withContext(
                Dispatchers.IO
            ) {
                onReceiveData.onReceiveNetworkData(
                    saved,
                    address
                )
            }
        }

        mBuffer = ByteArray(
            mBuffer.size
        )
    }

}