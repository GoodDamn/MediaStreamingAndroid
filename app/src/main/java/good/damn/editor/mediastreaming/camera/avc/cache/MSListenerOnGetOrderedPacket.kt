package good.damn.editor.mediastreaming.camera.avc.cache

interface MSListenerOnGetOrderedPacket {
    fun onGetOrderedPacket(
        frame: MSPacket
    )
}