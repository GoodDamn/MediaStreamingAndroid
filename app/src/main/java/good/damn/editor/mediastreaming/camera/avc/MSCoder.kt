package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.util.Log
import good.damn.editor.mediastreaming.network.MSStateable

abstract class MSCoder
: MediaCodec.Callback(),
MSStateable {

    companion object {
        private const val TAG = "MSCoder"
        const val TYPE_AVC = "video/avc"
    }

    protected abstract val mCoder: MediaCodec

    var isUninitialized = false
        private set

    override fun start() {
        isUninitialized = false
        mCoder.start()
    }

    override fun release() {
        isUninitialized = true
        mCoder.release()
        Log.d(TAG, "release: ")
    }

    override fun stop() {
        isUninitialized = true
        mCoder.reset()
    }
}