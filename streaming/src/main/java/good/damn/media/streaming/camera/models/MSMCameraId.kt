package good.damn.media.streaming.camera.models

import android.hardware.camera2.CameraCharacteristics

data class MSMCameraId(
    val logical: String,
    val physical: String? = null,
    var isLegacy: Boolean = false,
    val characteristics: CameraCharacteristics
) {
    override fun toString() = "$logical:$physical"

    val preciseId: String
        get() = physical ?: logical
}