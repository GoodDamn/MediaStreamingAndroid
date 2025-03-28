package good.damn.media.streaming.network.server.udp

import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.avc.cache.MSIOnEachMissedPacket
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.setIntegerOnPosition
import good.damn.media.streaming.extensions.setShortOnPosition
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MSPacketMissingHandler
: MSIOnEachMissedPacket {

    private val mSocket = DatagramSocket()

    var host: InetAddress? = null

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
        val buffer = ByteArray(6)
        val packet = DatagramPacket(
            buffer,
            0,
            buffer.size,
            host,
            MSStreamConstants.PORT_VIDEO_RESTORE_REQUEST
        )

        buffer.setIntegerOnPosition(
            frameId,
            0
        )

        buffer.setShortOnPosition(
            packetId.toInt(),
            4
        )

        packet.setData(
            buffer,
            0,
            buffer.size
        )

        mSocket.send(packet)
    }
}