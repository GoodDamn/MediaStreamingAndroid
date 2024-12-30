package good.damn.editor.mediastreaming.camera.avc.cache

interface MSListenerOnCombinePacket {
    fun onCombinePacket(
        packetId: Int,
        frame: MSPacketFrame
    )
}