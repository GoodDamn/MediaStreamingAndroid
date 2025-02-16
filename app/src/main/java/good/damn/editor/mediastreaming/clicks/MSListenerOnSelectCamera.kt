package good.damn.editor.mediastreaming.clicks

import good.damn.media.streaming.camera.models.MSCameraModelID


interface MSListenerOnSelectCamera {
    fun onSelectCamera(
        cameraId: MSCameraModelID
    )
}