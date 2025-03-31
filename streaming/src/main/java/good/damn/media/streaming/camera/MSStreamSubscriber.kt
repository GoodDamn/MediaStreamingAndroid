package good.damn.media.streaming.camera

interface MSStreamSubscriber {

    fun onGetPacket(
        data: ByteArray
    )
}