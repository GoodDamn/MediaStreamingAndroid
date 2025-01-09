package good.damn.editor.mediastreaming.camera.avc

import android.graphics.Camera
import android.hardware.camera2.CameraCharacteristics
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore.Audio.Media
import android.view.Surface
import good.damn.editor.mediastreaming.camera.MSCamera
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.camera2.getRotation
import good.damn.editor.mediastreaming.network.MSStateable

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
        width: Int,
        height: Int,
        character: CameraCharacteristics
    ) {
        mEncoder.configure(
            MediaFormat.createVideoFormat(
                MSCoder.TYPE_AVC,
                width,
                height
            ).apply {
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities
                        .COLOR_FormatSurface
                )

                setInteger(
                    MediaFormat.KEY_BIT_RATE,
                    1024 * 1024 * 1
                )

                setInteger(
                    MediaFormat.KEY_FRAME_RATE,
                    24
                )

                setInteger(
                    MediaFormat.KEY_ROTATION,
                    character.getRotation() ?: 0
                )

                setInteger(
                    MediaFormat.KEY_I_FRAME_INTERVAL,
                    1
                )
            }
        )
    }

    fun stop() {
        isRunning = false

        mCamera.stop()
        mEncoder.stop()

        mCamera.surfaces?.forEach {
            it.release()
        }
    }

    fun start(
        cameraId: MSCameraModelID
    ) {
        isRunning = true
        mCamera.apply {
            surfaces = arrayListOf(
                mEncoder.createInputSurface()
            )
            openCameraStream(
                cameraId
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