package good.damn.editor.mediastreaming.camera.avc.listeners

import java.nio.ByteBuffer

interface MSListenerOnGetFrameData {
    fun onGetFrameData(
        bufferData: ByteBuffer,
        offset: Int,
        len: Int
    )
}