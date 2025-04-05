package good.damn.media.streaming.camera.avc

import android.graphics.Camera
import android.hardware.camera2.CameraCharacteristics
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore.Audio.Media
import android.view.Surface
import good.damn.media.streaming.camera.MSCamera
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.camera2.default
import good.damn.media.streaming.extensions.camera2.getRotation

class MSCameraAVC(
    manager: MSManagerCamera,
    callbackFrame: MSListenerOnGetFrameData
) {

    companion object {
        private const val TAG = "MSCameraAVC"
    }

    private val mCamera = MSCamera(
        manager
    )

    private val mEncoder = MSEncoderAvc().apply {
        onGetFrameData = callbackFrame
    }

    var isRunning = false
        private set

    fun configure(
        mediaFormat: MediaFormat,
        handler: Handler
    ) = handler.post {
        mEncoder.configure(
            mediaFormat
        )
    }

    fun stop() {
        isRunning = false

        mCamera.stop()
        mEncoder.stop()
    }

    fun start(
        cameraId: MSCameraModelID,
        handler: Handler
    ) = handler.post {
        isRunning = true
        mCamera.apply {
            surfaces = arrayListOf(
                mEncoder.createInputSurface()
            )
            openCameraStream(
                cameraId,
                handler
            )
        }

        mEncoder.start()
    }

    fun release() {
        isRunning = false
        mEncoder.release()

        mCamera.apply {
            surfaces?.forEach {
                it.release()
            }

            release()
        }
    }

}