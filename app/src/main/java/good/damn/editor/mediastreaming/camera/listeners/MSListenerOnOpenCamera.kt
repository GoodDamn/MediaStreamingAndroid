package good.damn.editor.mediastreaming.camera.listeners

import android.hardware.camera2.CameraDevice

interface MSListenerOnOpenCamera {
    fun onOpenCamera(
        camera: CameraDevice
    )
}