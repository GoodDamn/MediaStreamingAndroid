package good.damn.editor.mediastreaming.camera.avc.cache

class MSPacketCombiner {

    private val mPackets = HashMap<
        Int, MSPacketFrame
    >()

    fun write(
        packetId: Int,
        chunkId: Short,
        chunkCount: Short,
        data: ByteArray,
        onCombinePacket: MSListenerOnCombinePacket
    ) {
        mPackets[
            packetId
        ]?.apply {

            chunks[
                chunkId
            ] = MSPacket(
                data
            )

            if (chunks.size.toShort() == chunkCount) {
                onCombinePacket.onCombinePacket(
                    packetId,
                    this
                )
                mPackets.remove(
                    packetId
                )
            }

            return
        }

        mPackets[
            packetId
        ] = MSPacketFrame(
            hashMapOf(
                chunkId to MSPacket(
                    data
                )
            )
        )
    }

}