package good.damn.editor.mediastreaming.camera

interface MSStreamSubscriber {
    fun onGetPacket(
        data: ByteArray
    )
}