package good.damn.editor.mediastreaming.camera.avc.cache

interface MSListenerOnGetOrderedFrame {
    fun onGetOrderedFrame(
        frame: MSFrame
    )
}