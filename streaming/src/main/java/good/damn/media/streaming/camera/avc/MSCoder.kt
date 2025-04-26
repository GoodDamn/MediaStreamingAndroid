package good.damn.media.streaming.camera.avc

import android.media.MediaCodec
import android.media.MediaFormat
import good.damn.media.streaming.network.MSStateable

abstract class MSCoder
: MSStateable {

    companion object {
        private const val TAG = "MSCoder"
        const val MIMETYPE_CODEC = MediaFormat.MIMETYPE_VIDEO_AVC
    }

    abstract val codec: MediaCodec

    var isRunning = false
        private set

    override fun start() {
        isRunning = true
        codec.start()
    }

    override fun release() {
        isRunning = false
        codec.release()
    }

    override fun stop() {
        isRunning = false
        codec.reset()
    }
}