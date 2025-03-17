package good.damn.media.streaming.network.server

import android.util.Log
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.media.streaming.extensions.integer
import good.damn.media.streaming.extensions.short
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveData
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

class MSReceiverCameraFrameRestore
: MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverCameraFrameRe"
    }

    var bufferizer: MSPacketBufferizer? = null

    var port: Int
        get() = mPacket.port
        set(v) {
            mPacket.port = v
        }

    var host: InetAddress
        get() = mPacket.address
        set(v) {
            mPacket.address = v
        }


    private val mSocket = DatagramSocket()

    private val mPacket = DatagramPacket(
        ByteArray(0),
        0,
        0
    ).apply {
        port = 6667
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val frameId = data.integer(0)
        val packetId = data.short(4)

        val frame = bufferizer?.getFrameById(
            frameId
        ) ?: return

        val packet = frame.packets.getOrNull(
            packetId
        ) ?: return

        Log.d(TAG, "onReceiveData: $frameId: $packetId")

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