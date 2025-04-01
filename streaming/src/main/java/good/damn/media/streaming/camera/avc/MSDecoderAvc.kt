package good.damn.media.streaming.camera.avc

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.Surface
import good.damn.media.streaming.camera.MSCameraCallbackDecoder
import good.damn.media.streaming.camera.avc.cache.MSFrame

class MSDecoderAvc
: MSCoder() {

    companion object {
        private const val TAG = "MSDecoderAvc"
    }

    // may throws Exception with no h264 codec
    override val mCoder = MediaCodec.createDecoderByType(
        TYPE_AVC
    )

    private val mCallbackDecoder = MSCameraCallbackDecoder()

    var isConfigured = false
        private set

    override fun stop() {
        isConfigured = false
        super.stop()
    }

    override fun release() {
        isConfigured = false
        super.release()
    }

    override fun start() {
        Log.d(TAG, "start: $isConfigured")
        if (!isConfigured) {
            return
        }
        super.start()
    }

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) = mCoder.run {
        isConfigured = true

        setCallback(
            mCallbackDecoder
        )

        Log.d(TAG, "dfg: configure: ")

        configure(
            format,
            decodeSurface,
            null,
            0
        )
    }

    fun addFrame(
        frame: MSFrame
    ) = mCallbackDecoder.addFrame(
        frame
    )
    
}