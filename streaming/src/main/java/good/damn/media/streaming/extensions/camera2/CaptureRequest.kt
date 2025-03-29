package good.damn.media.streaming.extensions.camera2

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest

inline fun CaptureRequest.Builder.default(
    characteristics: CameraCharacteristics
) {
    set(
        CaptureRequest.CONTROL_AE_MODE,
        CaptureRequest.CONTROL_AE_MODE_ON
    )

    set(
        CaptureRequest.CONTROL_AF_MODE,
        CaptureRequest.CONTROL_AF_MODE_AUTO
    )

    set(
        CaptureRequest.CONTROL_AWB_MODE,
        CaptureRequest.CONTROL_AWB_MODE_AUTO
    )

    characteristics.get(
        CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE
    )?.apply {
        set(
            CaptureRequest.SENSOR_SENSITIVITY,
            (lower + (upper - lower) * 0.2f).toInt()
        )
    }
}