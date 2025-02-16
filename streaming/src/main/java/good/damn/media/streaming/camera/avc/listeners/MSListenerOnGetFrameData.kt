package good.damn.media.streaming.camera.avc.listeners

import java.nio.ByteBuffer

interface MSListenerOnGetFrameData {
    fun onGetFrameData(
        bufferData: ByteBuffer,
        offset: Int,
        len: Int
    )
}