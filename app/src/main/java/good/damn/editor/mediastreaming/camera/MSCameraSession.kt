package good.damn.editor.mediastreaming.camera

import android.hardware.camera2.CameraCaptureSession
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
            // set capture FPS
            set(
                CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                Range.create(24,24)
            )

            // automatic exposure
            set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )

            // automatic white balance
            set(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_AUTO
            )
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