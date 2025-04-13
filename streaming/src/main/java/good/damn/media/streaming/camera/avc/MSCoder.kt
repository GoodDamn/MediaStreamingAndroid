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

    protected abstract val mCoder: MediaCodec

    var isRunning = false
        private set

    override fun start() {
        isRunning = true
        mCoder.start()
    }

    override fun release() {
        isRunning = false
        mCoder.release()
    }

    override fun stop() {
        isRunning = false
        mCoder.reset()
    }
}