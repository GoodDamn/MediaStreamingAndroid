package good.damn.editor.mediastreaming.camera.avc

import android.hardware.camera2.CameraCharacteristics
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import good.damn.editor.mediastreaming.camera.MSCamera
import good.damn.editor.mediastreaming.camera.avc.MSEncoderAvc.Companion.TYPE_AVC
import good.damn.editor.mediastreaming.extensions.camera2.getRotation
import good.damn.editor.mediastreaming.network.MSStateable

class MSCameraAVC: MSStateable {

    companion object {
        private const val TAG = "MSCameraAVC"
    }

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

    fun createEncodeSurface() =
        mEncoder.createInputSurface()

    override fun stop() {
        isRunning = false
        mEncoder.stop()
        mDecoder.stop()
    }

    override fun start() {
        isRunning = true
        mEncoder.start()
        mDecoder.start()
    }

    override fun release() {
        isRunning = false
        mEncoder.release()
        mDecoder.release()
    }

}