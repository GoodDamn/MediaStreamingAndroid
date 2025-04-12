package good.damn.media.streaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import good.damn.media.streaming.camera.MSCameraCallbackEncoder
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData

class MSEncoderAvc
: MSCoder() {

    companion object {
        private const val TAG = "MSEncoderAvc"
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createEncoderByType(
        MIME_TYPE_CODEC
    )

    private val mCallbackEncoder = MSCameraCallbackEncoder()

    var onGetFrameData: MSListenerOnGetFrameData?
        get() = mCallbackEncoder.onGetFrameData
        set(v) {
            mCallbackEncoder.onGetFrameData = v
        }

    fun configure(
        format: MediaFormat
    ) = mCoder.run {
        setCallback(
            mCallbackEncoder
        )

        configure(
            format,
            null,
            null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )
    }

    fun createInputSurface() = mCoder.createInputSurface()
}