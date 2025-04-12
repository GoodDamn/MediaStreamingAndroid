package good.damn.editor.mediastreaming.clicks

import android.util.Size
import good.damn.media.streaming.camera.models.MSMCameraId

interface MSListenerOnSelectResolution {
    fun onSelectResolution(
        resolution: Size,
        cameraId: MSMCameraId
    )
}