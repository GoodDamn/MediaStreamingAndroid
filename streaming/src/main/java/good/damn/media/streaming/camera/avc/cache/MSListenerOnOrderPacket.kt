package good.damn.media.streaming.camera.avc.cache

fun interface MSListenerOnOrderPacket {
    fun onOrderPacket(
        currentFrameId: Int
    )
}