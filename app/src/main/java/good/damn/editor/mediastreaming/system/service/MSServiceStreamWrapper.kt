package good.damn.editor.mediastreaming.system.service

import android.content.Context
import android.content.Intent
import android.health.connect.datatypes.HeightRecord
import android.util.Log
import good.damn.media.streaming.camera.models.MSCameraModelID

class MSServiceStreamWrapper {

    val serviceConnectionStream = MSCameraServiceConnection()

    companion object {
        private const val TAG = "MSServiceStreamWrapper"
    }

    var isStarted = false
        private set

    var isBound = false
        private set

    fun start(
        context: Context?
    ) {
        if (isStarted) {
            return
        }

        isStarted = true

        context?.apply {
            startService(
                intentStream(this)
            )
        }
    }

    fun bindCamera(
        context: Context?,
        host: String?,
        width: Int,
        height: Int,
        physicalId: String?,
        logicalId: String
    ) {
        context ?: return
        if (isBound) {
            return
        }
        isBound = true

        val intent = intentStream(
            context
        ).apply {
            putExtra(
                MSServiceStream.EXTRA_HOST,
                host
            )

            putExtra(
                MSServiceStream.EXTRA_VIDEO_WIDTH,
                width
            )

            putExtra(
                MSServiceStream.EXTRA_VIDEO_HEIGHT,
                height
            )

            putExtra(
                MSServiceStream.EXTRA_CAMERA_ID_PHYSICAL,
                physicalId
            )

            putExtra(
                MSServiceStream.EXTRA_CAMERA_ID_LOGICAL,
                logicalId
            )
        }

        context.bindService(
            intent,
            serviceConnectionStream,
            Context.BIND_AUTO_CREATE
        )
    }

    fun unbindCamera(
        context: Context?
    ) {
        Log.d(TAG, "unbindCamera: $isBound")
        if (!isBound) {
            return
        }
        isBound = false

        context?.unbindService(
            serviceConnectionStream
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