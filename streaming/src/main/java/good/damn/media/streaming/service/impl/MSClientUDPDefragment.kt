package good.damn.media.streaming.service.impl

import good.damn.media.streaming.camera.MSStreamCameraInputFrame
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.network.client.MSClientUDP
import java.nio.ByteBuffer

class MSClientUDPDefragment(
    private val bufferizer: MSPacketBufferizer
): MSStreamCameraInputFrame,
MSListenerOnEachDefragmentedPacket {

    var userId = 0
    var client: MSClientUDP? = null

    override fun onGetCameraFrame(
        frameId: Int,
        data: ByteBuffer,
        offset: Int,
        len: Int
    ) {
        MSStreamPacketFragmentizer.defragmentByteArray(
            userId,
            frameId,
            data,
            offset,
            len,
            this
        )

        if (frameId >= MSPacketBufferizer.CACHE_PACKET_SIZE) {
            bufferizer.removeFirstFrameQueueByFrameId(
                frameId
            )
        }
    }

    override fun onEachDefragmentedPacket(
        frameId: Int,
        packetId: Short,
        packetCount: Short,
        data: ByteArray
    ) {
        bufferizer.write(
            frameId,
            packetId,
            packetCount,
            data
        )

        client?.send(data)
    }

}