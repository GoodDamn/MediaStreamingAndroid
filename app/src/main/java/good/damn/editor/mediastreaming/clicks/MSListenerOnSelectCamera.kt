package good.damn.editor.mediastreaming.clicks

import good.damn.editor.mediastreaming.camera.models.MSCameraModelID

interface MSListenerOnSelectCamera {
    fun onSelectCamera(
        cameraId: MSCameraModelID
    )
}