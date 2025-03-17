package good.damn.media.streaming.network.server

import good.damn.media.streaming.camera.avc.cache.MSIOnEachMissedPacket
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.setIntegerOnPosition
import good.damn.media.streaming.extensions.setShortOnPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MSPacketMissingHandler {

    var bufferizer: MSPacketBufferizer? = null
    var isRunning = false

    fun handlingMissedPackets(
        host: InetAddress
    ) = CoroutineScope(
        Dispatchers.IO
    ).launch {
        isRunning = true
        val buffer = ByteArray(6)
        val socket = DatagramSocket()
        val packet = DatagramPacket(
            buffer,
            0,
            buffer.size,
            host,
            5555
        )

        val onEach = MSIOnEachMissedPacket { frameId, packetId ->
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

            socket.send(packet)
        }

        while (isRunning) {
            bufferizer?.findFirstMissingPacket(
                onEach
            )
        }
    }
}