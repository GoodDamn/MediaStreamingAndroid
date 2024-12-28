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
    surface: Surface
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
        surface
    )

    init {
        camera.surfaces = arrayListOf(
            mEncode.inputSurface
        )

        mEncode.onGetFrameData = mDecoder
    }

    fun start() {
        mEncode.start()
        mDecoder.start()
    }

    fun release() {
        mEncode.release()
        mDecoder.release()
    }

}