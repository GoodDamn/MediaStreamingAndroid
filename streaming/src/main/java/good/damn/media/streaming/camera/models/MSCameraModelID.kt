package good.damn.media.streaming.camera.models

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

data class MSCameraModelID(
    val logical: String,
    val physical: String? = null,
    var isLegacy: Boolean = false,
    val characteristics: CameraCharacteristics
) {
    override fun toString() = "$logical:$physical"

    val preciseId: String
        get() = physical ?: logical
}