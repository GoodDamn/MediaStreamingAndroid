package good.damn.media.streaming.network.client

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MSClientUDP(
    port: Int
) {
    companion object {
        private val TAG = MSClientUDP::class.simpleName
    }

    var host: InetAddress?
        get() = mPacket.address
        set(v) {
            mPacket.address = v
        }

    private val mSocket = DatagramSocket()

    private val mPacket = DatagramPacket(
        ByteArray(0),
        0
    ).apply {
        this.port = port
    }

    fun send(
        data: ByteArray
    ) {
        mPacket.setData(
            data,
            0,
            data.size
        )

        mSocket.send(
            mPacket
        )
    }

    fun release() {
        try {
            mSocket.close()
        } catch (e: Exception) {
            Log.d(TAG, "release: ${e.message}")
        }
    }
}