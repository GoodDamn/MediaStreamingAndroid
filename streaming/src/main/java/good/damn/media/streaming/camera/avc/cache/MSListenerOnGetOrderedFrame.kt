package good.damn.media.streaming.camera.avc.cache

interface MSListenerOnGetOrderedFrame {
    fun onGetOrderedFrame(
        frame: MSFrame
    )
}