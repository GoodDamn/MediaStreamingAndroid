package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import good.damn.editor.mediastreaming.network.MSStateable

abstract class MSCoder
: MediaCodec.Callback(),
MSStateable {

    companion object {
        const val TYPE_AVC = "video/avc"
    }

    protected abstract val mCoder: MediaCodec

    var isUnitialized = false
        private set

    override fun start() {
        isUnitialized = false
        mCoder.start()
    }

    override fun release() {
        isUnitialized = true
        mCoder.release()
    }

    override fun stop() {
        isUnitialized = true
        mCoder.reset()
    }
}