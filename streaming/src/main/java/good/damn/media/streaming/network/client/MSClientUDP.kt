package good.damn.media.streaming.network.client

import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.MSStateable
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

    fun sendToStream(
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
        mSocket.apply {
            disconnect()
            close()
        }
    }
}