package good.damn.editor.mediastreaming.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameData
import good.damn.editor.mediastreaming.misc.HandlerExecutor
import java.util.LinkedList

class MSCamera(
    context: Context
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

    private val manager = MSManagerCamera(
        context
    )

    private val mCameraId = manager.getCameraId(
        CameraCharacteristics.LENS_FACING,
        CameraMetadata.LENS_FACING_BACK
    ).firstOrNull()

    private val mCameraStream = MSCameraSession()

    private val mResolutionCamera = mCameraId?.run {
        manager.getOutputSizes(
            this,
            ImageFormat.JPEG
        )
    }

    private val mReader: ImageReader

    val rotation = mCameraId?.run {
        manager.getRotationInitial(
            this
        )
    } ?: 0

    var onGetCameraFrame: MSListenerOnGetCameraFrameData? = null

    init {
        val minRes = Size(1280, 720)

        Log.d(TAG, "init: $minRes RESOLUTIONS: ${mResolutionCamera?.contentToString()}")

        mReader = ImageReader.newInstance(
            minRes.width,
            minRes.height,
            ImageFormat.JPEG,
            1
        ).apply {
            setOnImageAvailableListener(
                this@MSCamera,
                Handler(
                    thread.looper
                )
            )
        }
    }

    fun openCameraStream() {
        Log.d(TAG, "openCameraStream: $mCameraId ROT: $rotation")
        mCameraStream.targets = listOf(
            mReader.surface
        )
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

    fun stop() {
        mCameraStream.stop()
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

    override fun onImageAvailable(
        reader: ImageReader?
    ) {
        val image = mReader.acquireLatestImage()

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