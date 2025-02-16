package good.damn.media.streaming.extensions.camera2

import android.hardware.camera2.CameraCharacteristics
import android.util.Range

inline fun CameraCharacteristics.getRotation() = get(
    CameraCharacteristics.SENSOR_ORIENTATION
)

inline fun CameraCharacteristics.getConfigurationMap() = get(
    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
)

inline fun CameraCharacteristics.getRangeFps(): Array<Range<Int>>? = get(
    CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES
)