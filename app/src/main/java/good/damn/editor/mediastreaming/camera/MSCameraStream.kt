package good.damn.editor.mediastreaming.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraMetadata
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface

class MSCameraStream
: CameraCaptureSession.StateCallback() {

    companion object {
        private val TAG = MSCameraStream::class.simpleName
    }

    var targets: List<Surface>? = null
    var streamUseCase: Long = 0x5 // CameraMetadata.VIDEO_CALL
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
        )

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

    fun release() {
        mSession?.stopRepeating()
        mSession = null
    }

}