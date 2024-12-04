package good.damn.editor.mediastreaming.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.misc.HandlerExecutor
import java.util.LinkedList
import java.util.concurrent.Executor

class MSCamera(
    context: Context
): CameraDevice.StateCallback() {

    companion object {
        private val TAG = MSCamera::class.simpleName
    }

    val thread = HandlerThread(
        "cameraDamn"
    ).apply {
        start()
    }

    private val manager = MSManagerCamera(
        context
    )

    private val mCameraId = manager.getCameraId(
        CameraCharacteristics.LENS_FACING,
        CameraMetadata.LENS_FACING_BACK
    ).firstOrNull()

    private val mCameraRotation = mCameraId?.run {
        manager.getRotationInitial(
            this
        )
    } ?: 0

    private val mCameraStream = MSCameraStream()

    fun openCameraStream(
        targets: List<Surface>
    ) {
        Log.d(TAG, "openCameraStream: $mCameraId ROT: $mCameraRotation")
        mCameraStream.targets = targets
        mCameraStream.handler = Handler(
            thread.looper
        )
        mCameraId?.apply {
            manager.openCamera(
                this,
                this@MSCamera
            )
        }
    }

    fun release() {
        mCameraStream.release()
        thread.quitSafely()
    }

    override fun onOpened(
        camera: CameraDevice
    ) {
        Log.d(TAG, "onOpened: ${mCameraStream.targets}")
        val targets = mCameraStream.targets
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val listConfig = LinkedList<OutputConfiguration>()

            targets.forEach {
                listConfig.add(
                    OutputConfiguration(
                        it
                    )
                )
            }

            camera.createCaptureSession(
                SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    listConfig,
                    HandlerExecutor(
                        mCameraStream.handler
                    ),
                    mCameraStream
                )
            )
            return
        }

        camera.createCaptureSession(
            targets,
            mCameraStream,
            mCameraStream.handler
        )
    }

    override fun onDisconnected(
        camera: CameraDevice
    ) {
        Log.d(TAG, "onDisconnected: $camera")
    }

    override fun onError(
        camera: CameraDevice,
        error: Int
    ) {
        Log.d(TAG, "onError: $error")
    }

}