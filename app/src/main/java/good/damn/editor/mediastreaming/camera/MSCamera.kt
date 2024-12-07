package good.damn.editor.mediastreaming.camera

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.util.Size
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameData
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.camera2.getConfigurationMap
import good.damn.editor.mediastreaming.extensions.camera2.getRangeFps
import good.damn.editor.mediastreaming.extensions.camera2.getRotation
import good.damn.editor.mediastreaming.misc.HandlerExecutor
import java.util.LinkedList
import kotlin.math.log

class MSCamera(
    private val manager: MSManagerCamera
): CameraDevice.StateCallback(),
ImageReader.OnImageAvailableListener {

    companion object {
        private val TAG = MSCamera::class.simpleName
    }

    private val thread = HandlerThread(
        "cameraDamn"
    ).apply {
        start()
    }

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

    private val mCameraStream = MSCameraSession()

    private var mCurrentDevice: Device? = null

    var rotation = 0
        private set

    var resolutions: Array<Size>? = null
        private set

    var fpsRanges: Array<Range<Int>>? = null
        private set

    var characteristics: CameraCharacteristics? = null
        private set

    var onGetCameraFrame: MSListenerOnGetCameraFrameData? = null

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

        val minRes = Size(640, 480)

        mCameraStream.handler = Handler(
            thread.looper
        )

        mCurrentDevice = Device(
            cameraId,
            ImageReader.newInstance(
                minRes.width,
                minRes.height,
                ImageFormat.JPEG,
                1
            ).apply {
                setOnImageAvailableListener(
                    this@MSCamera,
                    mCameraStream.handler
                )
            }
        ).apply {
            mCameraStream.targets = listOf(
                reader.surface
            )
        }

        manager.openCamera(
            cameraId.logical,
            this@MSCamera,
            mCameraStream.handler
        )

        return true
    }

    fun stop() {
        mCameraStream.stop()
        mCurrentDevice?.apply {
            device?.close()
            reader.close()
        }
    }

    fun release() {
        mCameraStream.release()
        thread.quitSafely()
    }

    override fun onOpened(
        camera: CameraDevice
    ) {
        mCurrentDevice?.device = camera
        Log.d(TAG, "onOpened: ${mCameraStream.targets}")
        val targets = mCameraStream.targets
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val listConfig = LinkedList<OutputConfiguration>()

            targets.forEach {
                listConfig.add(
                    OutputConfiguration(
                        it
                    ).apply {
                        setPhysicalCameraId(
                            cameraId?.physical
                        )
                    }
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

    override fun onImageAvailable(
        reader: ImageReader
    ) {

        val image = reader.acquireLatestImage()

        onGetCameraFrame?.onGetFrame(
            image.planes[0]
        )

        image.close()
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
    val reader: ImageReader,
    var device: CameraDevice? = null
)