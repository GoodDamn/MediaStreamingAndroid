package good.damn.editor.mediastreaming.system.service

import android.content.Context
import android.content.Intent
import android.util.Log
import good.damn.editor.mediastreaming.extensions.supportsForegroundService
import good.damn.editor.mediastreaming.system.service.serv.MSServiceStream
import good.damn.editor.mediastreaming.system.service.serv.MSServiceStreamForeground
import good.damn.media.streaming.service.MSCameraServiceConnection
import good.damn.media.streaming.service.impl.MSListenerOnConnectUser
import good.damn.media.streaming.service.impl.MSListenerOnSuccessHandshake
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo

class MSServiceStreamWrapper {

    private val mServiceConnectionStream = MSCameraServiceConnection()

    companion object {
        private const val TAG = "MSServiceStreamWrapper"
    }

    var onConnectUser: MSListenerOnConnectUser?
        get() = mServiceConnectionStream.onConnectUser
        set(v) {
            mServiceConnectionStream.onConnectUser = v
        }

    var isStarted = false
        private set

    var isBound = false
        private set

    var isStreamingVideo = false
        private set

    fun requestConnectedUsers() = mServiceConnectionStream
        .requestConnectedUsers()

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo,
        onSuccessHandshake: MSListenerOnSuccessHandshake
    ) = mServiceConnectionStream.run {
        this.onSuccessHandshake = onSuccessHandshake
        sendHandshakeSettings(
            model
        )

        isStreamingVideo = true
    }

    fun setCanSendFrames(
        canReceive: Boolean
    ) = mServiceConnectionStream.setCanSendFrames(
        canReceive
    )

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

        if (context.supportsForegroundService()) {
            context.startForegroundService(
                intentStream(context)
            )
            return
        }

        context.startService(
            intentStream(context)
        )
    }

    fun bind(
        context: Context?
    ) {
        context ?: return
        Log.d(TAG, "bind: $isBound:${isStarted}")
        if (isBound || !isStarted) {
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
        Log.d(TAG, "unbindCamera: $isBound $isStarted $context")
        context ?: return

        if (!isBound || !isStarted) {
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
) = if (
    context.supportsForegroundService()
) Intent(
    context,
    MSServiceStreamForeground::class.java
) else Intent(
    context,
    MSServiceStream::class.java
)