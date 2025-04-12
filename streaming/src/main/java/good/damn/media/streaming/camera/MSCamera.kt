package good.damn.media.streaming.camera

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.Surface
import good.damn.media.streaming.camera.models.MSMCameraId
import good.damn.media.streaming.misc.HandlerExecutor
import java.util.LinkedList

class MSCamera(
    private val manager: MSManagerCamera
): CameraDevice.StateCallback() {

    companion object {
        private val TAG = MSCamera::class.simpleName
    }

    private val mCameraSession = MSCameraSession()

    private var mCurrentDevice: Device? = null

    var surfaces: MutableList<Surface>?
        get() = mCameraSession.targets
        set(v) {
            mCameraSession.targets = v
        }

    var camera: MSMCameraId? = null
        private set

    fun openCameraStream(
        cameraId: MSMCameraId,
        handler: Handler
    ): Boolean {
        Log.d(TAG, "openCameraStream: $cameraId")

        mCameraSession.characteristics = manager.getCharacteristics(
            cameraId.preciseId
        )

        mCameraSession.handler = handler

        if (mCurrentDevice?.id == cameraId) {
            Log.d(TAG, "openCameraStream: $cameraId is current opened device. Dismissed")
            stop()
        }

        camera = cameraId

        mCurrentDevice = Device(
            cameraId
        )

        manager.openCamera(
            cameraId,
            this@MSCamera,
            mCameraSession.handler
        )

        return true
    }

    fun stop() {
        release()
        mCurrentDevice?.apply {
            device?.close()
        }
    }

    fun release() {
        mCameraSession.release()
    }

    override fun onOpened(
        camera: CameraDevice
    ) {
        mCurrentDevice?.device = camera
        Log.d(TAG, "onOpened: ${this.camera}")
        val targets = mCameraSession.targets
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val listConfig = LinkedList<
                OutputConfiguration
            >()

            targets.forEach {
                listConfig.add(
                    OutputConfiguration(
                        it
                    ).apply {
                        setPhysicalCameraId(
                            this@MSCamera.camera?.physical
                        )
                    }
                )
            }

            camera.createCaptureSession(
                SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    listConfig,
                    HandlerExecutor(
                        mCameraSession.handler
                    ),
                    mCameraSession
                )
            )
            return
        }

        camera.createCaptureSession(
            targets,
            mCameraSession,
            mCameraSession.handler
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

private data class Device(
    val id: MSMCameraId,
    var device: CameraDevice? = null
)