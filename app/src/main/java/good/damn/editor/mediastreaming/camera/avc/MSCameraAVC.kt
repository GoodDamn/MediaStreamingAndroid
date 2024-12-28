package good.damn.editor.mediastreaming.camera.avc

import android.hardware.camera2.CameraCharacteristics
import android.view.Surface
import good.damn.editor.mediastreaming.camera.MSCamera
import good.damn.editor.mediastreaming.extensions.camera2.getRotation

class MSCameraAVC(
    width: Int,
    height: Int,
    camera: CameraCharacteristics
) {

    companion object {
        private const val TAG = "MSCameraAVC"
    }

    private val mEncoder: MSEncoderAvc

    private val mDecoder: MSDecoderAvc

    init {
        val rotation = camera.getRotation()
            ?: 0

        mEncoder = MSEncoderAvc(
            width,
            height,
            rotation
        )

        mDecoder = MSDecoderAvc(
            width,
            height,
            rotation
        )

        mEncoder.onGetFrameData = mDecoder
    }

    fun configure(
        decodeSurface: Surface
    ) {
        mEncoder.configure()

        mDecoder.configure(
            decodeSurface
        )
    }

    fun createEncodeSurface() =
        mEncoder.createInputSurface()

    fun start() {
        mEncoder.start()
        mDecoder.start()
    }

    fun release() {
        mEncoder.release()
        mDecoder.release()
    }

}