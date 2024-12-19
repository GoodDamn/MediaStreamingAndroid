package good.damn.editor.mediastreaming.camera

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.Surface
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.camera2.getConfigurationMap
import good.damn.editor.mediastreaming.extensions.camera2.getRangeFps
import good.damn.editor.mediastreaming.extensions.camera2.getRotation
import good.damn.editor.mediastreaming.misc.HandlerExecutor
import java.util.LinkedList

class MSCamera(
    private val manager: MSManagerCamera
): CameraDevice.StateCallback() {

    companion object {
        private val TAG = MSCamera::class.simpleName
    }

    private val thread = HandlerThread(
        "cameraDamn"
    ).apply {
        start()
    }

    private val mCameraSession = MSCameraSession().apply {
        handler = Handler(
            thread.looper
        )
    }

    private var mCurrentDevice: Device? = null

    var surfaces: List<Surface>?
        get() = mCameraSession.targets
        set(v) {
            mCameraSession.targets = v
        }

    var rotation = 0
        private set

    var resolutions: Array<Size>? = null
        private set

    var fpsRanges: Array<Range<Int>>? = null
        private set

    var characteristics: CameraCharacteristics? = null
        private set

    var cameraId: MSCameraModelID? = null
        set(v) {
            field = v
            v?.apply {
                characteristics = manager.getCharacteristics(
                    physical ?: logical
                ).apply {
                    rotation = getRotation() ?: 0

                    resolutions = getConfigurationMap()?.getOutputSizes(
                        ImageFormat.JPEG
                    )

                    fpsRanges = getRangeFps()

                    Log.d(TAG, "CAMERA_ID: ROTATION: $rotation")
                    Log.d(TAG, "CAMERA_ID: RES: ${resolutions.contentToString()}")
                    Log.d(TAG, "CAMERA_ID: FPS_RANGES: ${fpsRanges.contentToString()}")
                }
            }
        }

    fun openCameraStream(): Boolean {
        Log.d(TAG, "openCameraStream: $cameraId")

        val cameraId = cameraId
            ?: return false

        mCurrentDevice?.apply {
            if (id == cameraId) {
                Log.d(TAG, "openCameraStream: $cameraId is current opened device. Dismissed")
                return false
            }
        }

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
        mCameraSession.stop()
        mCurrentDevice?.apply {
            device?.close()
        }
    }

    fun release() {
        mCameraSession.release()
        thread.quitSafely()
    }

    override fun onOpened(
        camera: CameraDevice
    ) {
        mCurrentDevice?.device = camera
        Log.d(TAG, "onOpened: $cameraId")
        val targets = mCameraSession.targets
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