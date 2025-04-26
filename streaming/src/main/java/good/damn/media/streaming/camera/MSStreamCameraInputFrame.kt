package good.damn.media.streaming.camera

import java.nio.ByteBuffer

interface MSStreamCameraInputFrame {
    fun onGetCameraFrame(
        frameId: Int,
        data: ByteBuffer,
        offset: Int,
        len: Int
    )
}