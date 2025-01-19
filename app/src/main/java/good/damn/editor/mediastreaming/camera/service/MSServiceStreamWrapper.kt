package good.damn.editor.mediastreaming.camera.service

import android.content.Context
import android.content.Intent
import android.util.Log
import good.damn.editor.mediastreaming.camera.MSCamera
import good.damn.editor.mediastreaming.camera.avc.MSUtilsAvc
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID

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
                intentStream(context)
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
    intent: Intent
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
        MSUtilsAvc.VIDEO_WIDTH
    )

    putExtra(
        MSServiceStream.EXTRA_VIDEO_HEIGHT,
        MSUtilsAvc.VIDEO_HEIGHT
    )

    putExtra(
        MSServiceStream.EXTRA_HOST,
        host
    )
}
