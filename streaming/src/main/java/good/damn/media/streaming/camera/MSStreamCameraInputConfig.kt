package good.damn.media.streaming.camera

import java.nio.ByteBuffer

interface MSStreamCameraInputConfig {
    fun onGetCameraConfigStream(
        data: ByteBuffer,
        offset: Int,
        len: Int
    )
}