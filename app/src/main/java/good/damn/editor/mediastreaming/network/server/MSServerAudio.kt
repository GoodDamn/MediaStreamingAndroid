package good.damn.editor.mediastreaming.network.server

import android.util.Log
import good.damn.editor.mediastreaming.audio.MSAudioRecord
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerServerOnReceiveSamples
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket

class MSServerAudio {

    companion object {
        private val TAG = MSServerAudio::class.simpleName
    }

    private val mScope = CoroutineScope(
        Dispatchers.IO
    )

    var isRunning = false
        private set

    var onReceiveSamples: MSListenerServerOnReceiveSamples? = null

    private val mBuffer = ByteArray(
        MSAudioRecord.DEFAULT_BUFFER_SIZE
    )

    private var mCurrentSocket: DatagramSocket? = null

    fun start(
        port: Int
    )= mScope.launch {
        isRunning = true
        mCurrentSocket = DatagramSocket(
            port
        )
        while (
            listen()
        ) {}
    }

    fun stop() {
        isRunning = false
        mCurrentSocket?.close()
    }

    private inline fun listen(): Boolean {

        if (!isRunning) {
            return false
        }

        val packet = DatagramPacket(
            mBuffer,
            mBuffer.size
        )

        Log.d(TAG, "listen: ")
        mCurrentSocket?.receive(
            packet
        )

        Log.d(TAG, "listen: ${mBuffer.contentToString()}")

        onReceiveSamples?.onReceiveSamples(
            mBuffer,
            0,
            mBuffer.size
        )

        return true
    }

}