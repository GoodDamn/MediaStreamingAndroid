package good.damn.editor.mediastreaming.clicks

import android.util.Size
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID

interface MSListenerOnSelectResolution {
    fun onSelectResolution(
        resolution: Size,
        cameraId: MSCameraModelID
    )
}