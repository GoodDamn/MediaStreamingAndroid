package good.damn.media.streaming.camera

import java.nio.ByteBuffer

interface MSStreamCameraInputSubscriber {

    fun onGetCameraConfigStream(
        data: ByteBuffer,
        offset: Int,
        len: Int
    )

    fun onGetCameraFrame(
        frameId: Int,
        data: ByteBuffer,
        offset: Int,
        len: Int
    )
}