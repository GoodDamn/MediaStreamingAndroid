package good.damn.editor.mediastreaming.camera.listeners

import android.media.Image.Plane
import java.nio.Buffer
import java.nio.ByteBuffer

interface MSListenerOnGetCameraFrameData {
    fun onGetFrame(
        yPlane: Plane,
        uPlane: Plane,
        vPlane: Plane
    )
}