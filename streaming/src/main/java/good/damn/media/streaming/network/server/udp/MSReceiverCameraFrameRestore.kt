package good.damn.media.streaming.network.server.udp

import android.util.Log
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.integer
import good.damn.media.streaming.extensions.short
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveNetworkData
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MSReceiverCameraFrameRestore
: MSListenerOnReceiveNetworkData {

    companion object {
        private const val TAG = "MSReceiverCameraFrameRe"
    }

    var bufferizer: MSPacketBufferizer? = null

    var port: Int
        get() = mPacket.port
        set(v) {
            mPacket.port = v
        }

    private val mSocket = DatagramSocket()

    private val mPacket = DatagramPacket(
        ByteArray(0),
        0,
        0
    ).apply {
        port = 6667
    }

    override suspend fun onReceiveNetworkData(
        data: ByteArray,
        src: InetAddress
    ) {

        val frameId = data.integer(0)
        val packetId = data.short(4)

        val frame = bufferizer?.getFrameById(
            frameId
        ) ?: return

        if (frame.id != frameId) {
            return
        }

        val packet = frame.packets.getOrNull(
            packetId
        ) ?: return

        Log.d(TAG, "onReceiveData: $frameId:$packetId")

        mPacket.address = src

        mPacket.setData(
            packet.data,
            0,
            packet.data.size
        )

        mSocket.send(
            mPacket
        )
    }
}