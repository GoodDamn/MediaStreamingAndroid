package good.damn.media.streaming.camera.service

import android.content.Context
import android.content.Intent
import android.util.Log
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.models.MSCameraModelID

class MSServiceStreamWrapper {

    private var mServiceConnectionStream = MSCameraServiceConnection()

    companion object {
        private const val TAG = "MSServiceStreamWrapper"
    }

    var isStarted = false
        private set

    var isBound = false
        private set

    fun start(
        context: Context
    ) {
        if (isStarted) {
            return
        }

        isStarted = true
        context.startService(
            intentStream(context)
        )
    }

    fun destroy(
        context: Context
    ) {
        isStarted = false
        context.stopService(
            intentStream(context)
        )
    }

    fun bind(
        videoWidth: Int,
        videoHeight: Int,
        cameraId: MSCameraModelID,
        host: String,
        context: Context
    ) {
        if (isBound) {
            return
        }
        Log.d(TAG, "bind: $mServiceConnectionStream")
        isBound = true
        context.bindService(
            fillIntent(
                cameraId,
                host,
                intentStream(context),
                videoWidth,
                videoHeight
            ),
            mServiceConnectionStream,
            Context.BIND_AUTO_CREATE
        )

    }

    fun unbind(
        context: Context
    ) {
        if (!isBound) {
            return
        }
        isBound = false
        context.unbindService(
            mServiceConnectionStream
        )

        mServiceConnectionStream = MSCameraServiceConnection()
    }
}

private inline fun MSServiceStreamWrapper.intentStream(
    context: Context
) = Intent(
    context,
    MSServiceStream::class.java
)

private inline fun MSServiceStreamWrapper.fillIntent(
    cameraId: MSCameraModelID,
    host: String,
    intent: Intent,
    videoWidth: Int,
    videoHeight: Int
) = intent.apply {
    putExtra(
        MSServiceStream.EXTRA_CAMERA_ID_LOGICAL,
        cameraId.logical
    )

    putExtra(
        MSServiceStream.EXTRA_CAMERA_ID_PHYSICAL,
        cameraId.physical
    )

    putExtra(
        MSServiceStream.EXTRA_VIDEO_WIDTH,
        videoWidth
    )

    putExtra(
        MSServiceStream.EXTRA_VIDEO_HEIGHT,
        videoHeight
    )

    putExtra(
        MSServiceStream.EXTRA_HOST,
        host
    )
}
