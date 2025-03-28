package good.damn.media.streaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import good.damn.media.streaming.camera.MSCameraCallbackDecoder
import good.damn.media.streaming.camera.MSCameraCallbackEncoder
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.media.streaming.network.MSStateable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MSEncoderAvc
: MSCoder() {

    companion object {
        private const val TAG = "MSEncoderAvc"
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createEncoderByType(
        TYPE_AVC
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