package good.damn.editor.mediastreaming.camera.listeners

interface MSListenerOnGetCameraFrameData {
    fun onGetFrame(
        data: ByteArray
    )
}