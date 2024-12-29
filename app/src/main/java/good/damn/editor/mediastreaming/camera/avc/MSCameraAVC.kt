package good.damn.editor.mediastreaming.camera.avc

import android.hardware.camera2.CameraCharacteristics
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import good.damn.editor.mediastreaming.camera.MSCamera
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.avc.MSEncoderAvc.Companion.TYPE_AVC
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.camera2.getRotation
import good.damn.editor.mediastreaming.network.MSStateable

class MSCameraAVC(
    manager: MSManagerCamera
) {

    companion object {
        private const val TAG = "MSCameraAVC"
    }

    private val mCamera = MSCamera(
        manager
    )

    private val mDecoder = MSDecoderAvc()
    private val mEncoder = MSEncoderAvc().apply {
        onGetFrameData = mDecoder
    }

    var isRunning = false
        private set

    fun configure(
        width: Int,
        height: Int,
        camera: CameraCharacteristics,
        decodeSurface: Surface
    ) {
        mEncoder.configure(
            MediaFormat.createVideoFormat(
                TYPE_AVC,
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
                    2_000_000
                )

                setInteger(
                    MediaFormat.KEY_FRAME_RATE,
                    24
                )

                setInteger(
                    MediaFormat.KEY_ROTATION,
                    camera.getRotation() ?: 0
                )

                setInteger(
                    MediaFormat.KEY_I_FRAME_INTERVAL,
                    1
                )
            }
        )

        mDecoder.configure(
            decodeSurface,
            MediaFormat.createVideoFormat(
                TYPE_AVC,
                width,
                height
            ).apply {
                setInteger(
                    MediaFormat.KEY_ROTATION,
                    camera.getRotation() ?: 0
                )
            }
        )
    }

    fun stop() {
        isRunning = false

        mCamera.stop()
        mEncoder.stop()
        mDecoder.stop()

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
        mDecoder.start()
    }

    fun release() {
        isRunning = false
        mEncoder.release()
        mDecoder.release()

        mCamera.apply {
            surfaces?.forEach {
                it.release()
            }

            release()
        }
    }

}