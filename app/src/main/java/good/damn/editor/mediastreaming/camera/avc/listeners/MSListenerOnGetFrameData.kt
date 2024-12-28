package good.damn.editor.mediastreaming.camera.avc.listeners

interface MSListenerOnGetFrameData {
    fun onGetFrameData(
        bufferData: ByteArray,
        offset: Int,
        len: Int
    )
}