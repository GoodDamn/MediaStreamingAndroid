package good.damn.media.streaming.service.impl

interface MSListenerOnEachDefragmentedPacket {
    fun onEachDefragmentedPacket(
        frameId: Int,
        packetId: Short,
        packetCount: Short,
        data: ByteArray
    )
}