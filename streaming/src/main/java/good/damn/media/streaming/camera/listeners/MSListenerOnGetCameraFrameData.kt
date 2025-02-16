package good.damn.media.streaming.camera.listeners

import android.media.Image.Plane
import java.nio.Buffer
import java.nio.ByteBuffer

interface MSListenerOnGetCameraFrameData {
    fun onGetFrame(
        jpegPlane: Plane
    )
}