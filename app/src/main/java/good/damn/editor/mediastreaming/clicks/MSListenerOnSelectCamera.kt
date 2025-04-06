package good.damn.editor.mediastreaming.clicks

import android.view.View
import good.damn.media.streaming.camera.models.MSCameraModelID


interface MSListenerOnSelectCamera {
    fun onSelectCamera(
        v: View?,
        cameraId: MSCameraModelID
    )
}