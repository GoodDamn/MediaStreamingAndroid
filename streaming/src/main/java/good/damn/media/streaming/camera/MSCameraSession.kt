package good.damn.media.streaming.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Range
import android.view.Surface

class MSCameraSession
: CameraCaptureSession.StateCallback() {

    companion object {
        private val TAG = MSCameraSession::class.simpleName
    }

    var targets: MutableList<Surface>? = null

    var handler = Handler(
        Looper.getMainLooper()
    )

    var characteristics: CameraCharacteristics? = null

    private var mSession: CameraCaptureSession? = null

    private val mCameraCapture = MSCameraCapture()

    override fun onConfigured(
        session: CameraCaptureSession
    ) {
        mSession = session
        Log.d(TAG, "onConfigured: $targets")

        val request = session.device.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        ).apply {
            set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_OFF
            )

            set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF
            )

            set(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_OFF
            )

            characteristics?.get(
                CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE
            )?.apply {
                set(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    (lower + (upper - lower) * 0.5f).toInt()
                )
            }
        }

        targets?.forEach {
            request.addTarget(it)
        }

        session.setRepeatingRequest(
            request.build(),
            mCameraCapture,
            handler
        )
    }

    override fun onConfigureFailed(
        session: CameraCaptureSession
    ) {
        Log.d(TAG, "onConfigureFailed: $session ")
    }

    fun stop() {
        release()
    }

    fun release() {
        mSession?.apply {
            stopRepeating()
            close()
        }
        mSession = null
    }

}