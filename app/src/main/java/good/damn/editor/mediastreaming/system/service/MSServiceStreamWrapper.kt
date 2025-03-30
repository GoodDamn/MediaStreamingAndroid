package good.damn.editor.mediastreaming.system.service

import android.content.Context
import android.content.Intent

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
            isBound = true
            bindService(
                intentStream(this),
                serviceConnectionStream,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    fun destroy(
        context: Context
    ) {
        isStarted = false

        context.unbindService(
            serviceConnectionStream
        )

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