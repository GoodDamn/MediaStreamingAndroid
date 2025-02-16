package good.damn.editor.mediastreaming.clicks

import android.util.Size
import good.damn.media.streaming.camera.models.MSCameraModelID

interface MSListenerOnSelectResolution {
    fun onSelectResolution(
        resolution: Size,
        cameraId: MSCameraModelID
    )
}