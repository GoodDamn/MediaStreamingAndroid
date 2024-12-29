package good.damn.editor.mediastreaming.camera

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.misc.HandlerExecutor
import java.util.LinkedList

class MSCamera(
    private val manager: MSManagerCamera
): CameraDevice.StateCallback() {

    companion object {
        private val TAG = MSCamera::class.simpleName
    }

    private var mThread: HandlerThread? = null
    private var mHandler: Handler? = null

    private val mCameraSession = MSCameraSession()

    private var mCurrentDevice: Device? = null

    var surfaces: MutableList<Surface>?
        get() = mCameraSession.targets
        set(v) {
            mCameraSession.targets = v
        }

    var camera: MSCameraModelID? = null
        private set

    fun openCameraStream(
        cameraId: MSCameraModelID
    ): Boolean {
        Log.d(TAG, "openCameraStream: $cameraId")

        if (mCurrentDevice?.id == cameraId) {
            Log.d(TAG, "openCameraStream: $cameraId is current opened device. Dismissed")
            stop()
        }

        mThread = HandlerThread(
            "cameraThread"
        ).apply {
            start()

            mHandler = Handler(
                looper
            ).apply {
                mCameraSession.handler = this
            }
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
        mThread?.interrupt()
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
    val id: MSCameraModelID,
    var device: CameraDevice? = null
)