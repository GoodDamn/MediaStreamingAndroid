package good.damn.editor.mediastreaming.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraMetadata
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
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

    private val mCameraStream = MSCameraStream()

    fun openCameraStream(
        targets: List<Surface>
    ) {
        Log.d(TAG, "openCameraStream: $mCameraId")
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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            val listConfig = LinkedList<OutputConfiguration>()
//
//            targets.forEach {
//                listConfig.add(
//                    OutputConfiguration(it).apply {
//                        streamUseCase = mCameraStream.streamUseCase
//                    }
//                )
//            }
//
//            camera.createCaptureSession(
//                SessionConfiguration(
//                    SessionConfiguration.SESSION_REGULAR,
//                    listConfig,
//                    MSCameraExecutor(),
//                    mCameraStream
//                )
//            )
//            return
//        }

        camera.createCaptureSession(
            targets,
            mCameraStream,
            Handler(
                thread.looper
            )
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

private class MSCameraExecutor: Executor {
    override fun execute(
        command: Runnable?
    ) {
        command?.run()
    }

}