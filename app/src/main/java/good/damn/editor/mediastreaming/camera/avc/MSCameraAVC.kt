package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.camera.MSCamera
import java.io.ByteArrayOutputStream

class MSCameraAVC(
    width: Int,
    height: Int,
    camera: MSCamera,
    surfacePreview: Surface
) {

    companion object {
        private const val TAG = "MSCameraAVC"
    }

    private val mEncode = MSEncoderAvc(
        width,
        height,
        camera.rotation
    )

    private val mDecoder = MSDecoderAvc(
        width,
        height,
        camera.rotation,
        surfacePreview,
        mEncode.stream
    )

    init {
        camera.surfaces = arrayListOf(
            mEncode.inputSurface
        )
    }

    fun start() {
        mEncode.start()
    }

}