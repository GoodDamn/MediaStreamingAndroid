package good.damn.media.streaming.network.server.udp

import android.util.Log
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.avc.cache.MSIOnEachMissedPacket
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.setIntegerOnPosition
import good.damn.media.streaming.extensions.setShortOnPosition
import good.damn.media.streaming.network.client.MSClientUDP
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MSPacketMissingHandler
: MSIOnEachMissedPacket {

    companion object {
        private const val TAG = "MSPacketMissingHandler"
    }

    private val mClient = MSClientUDP(
        MSStreamConstants.PORT_VIDEO_RESTORE_REQUEST
    )

    var host: InetAddress?
        get() = mClient.host
        set(v) {
            mClient.host = v
        }

    fun handlingMissedPackets(
        bufferizer: MSPacketBufferizer?
    ) {
        bufferizer?.findFirstMissingPacket(
            this
        )
    }

    override fun onEachMissedPacket(
        frameId: Int,
        packetId: Short
    ) {
        Log.d(TAG, "onEachMissedPacket: $frameId:$packetId")
        val buffer = ByteArray(6)
        buffer.setIntegerOnPosition(
            frameId,
            0
        )

        buffer.setShortOnPosition(
            packetId.toInt(),
            4
        )
        mClient.sendToStream(
            buffer
        )
    }
}