package good.damn.editor.mediastreaming.system.service

import android.content.Context
import android.content.Intent
import android.util.Log
import good.damn.media.streaming.camera.models.MSCameraModelID

class MSServiceStreamWrapper {

    private val mServiceConnectionStream = MSCameraServiceConnection()

    companion object {
        private const val TAG = "MSServiceStreamWrapper"
    }

    var isStarted = false
        private set

    var isBound = false
        private set

    var isStreamingVideo = false
        private set

    fun startStreamingVideo(
        modelID: MSCameraModelID,
        width: Int,
        height: Int,
        host: String
    ) {
        mServiceConnectionStream.startStreamingVideo(
            modelID,
            width,
            height,
            host
        )
        isStreamingVideo = true
    }

    fun stopStreamingVideo() {
        mServiceConnectionStream
            .stopStreamingVideo()
        isStreamingVideo = false
    }

    fun startServiceStream(
        context: Context?
    ) {
        context ?: return

        if (isStarted) {
            return
        }

        isStarted = true

        context.startService(
            intentStream(context)
        )

    }

    fun bind(
        context: Context?
    ) {
        context ?: return
        if (isBound) {
            return
        }
        isBound = true

        context.bindService(
            intentStream(context),
            mServiceConnectionStream,
            Context.BIND_AUTO_CREATE
        )
    }

    fun unbind(
        context: Context?
    ) {
        Log.d(TAG, "unbindCamera: $isBound")
        context ?: return

        if (!isBound) {
            return
        }
        isBound = false

        context.unbindService(
            mServiceConnectionStream
        )
    }

    fun destroy(
        context: Context?
    ) {
        context ?: return

        if (!isStarted) {
            return
        }

        isStarted = false

        context.stopService(
            intentStream(context)
        )
    }

}

private inline fun intentStream(
    context: Context
) = Intent(
    context,
    MSServiceStream::class.java
)