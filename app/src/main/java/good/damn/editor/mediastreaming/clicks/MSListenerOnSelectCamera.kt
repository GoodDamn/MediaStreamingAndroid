package good.damn.editor.mediastreaming.clicks

import android.view.View
import good.damn.media.streaming.camera.models.MSMCameraId


interface MSListenerOnSelectCamera {
    fun onSelectCamera(
        v: View?,
        cameraId: MSMCameraId
    )
}