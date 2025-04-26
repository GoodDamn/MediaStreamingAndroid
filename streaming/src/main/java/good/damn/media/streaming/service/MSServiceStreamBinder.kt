package good.damn.media.streaming.service

import android.os.Binder
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo
import good.damn.media.streaming.service.impl.MSAccepterStreamConfigHandshake
import good.damn.media.streaming.service.impl.MSListenerOnConnectUser
import good.damn.media.streaming.service.impl.MSListenerOnSuccessHandshake
import good.damn.media.streaming.service.impl.MSServiceStreamImplHandshake
import good.damn.media.streaming.service.impl.MSServiceStreamImplVideo

class MSServiceStreamBinder(
    private val mUserId: Int,
    private val mImplVideo: MSServiceStreamImplVideo,
    private val mImplHandshake: MSServiceStreamImplHandshake,
    private val mAccepterStreamConfig: MSAccepterStreamConfigHandshake
): Binder() {

    var onConnectUser: MSListenerOnConnectUser?
        get() = mImplHandshake.onConnectUser
        set(v) {
            mImplHandshake.onConnectUser = v
        }

    var onSuccessHandshake: MSListenerOnSuccessHandshake?
        get() = mImplHandshake.onSuccessHandshake
        set(v) {
            mImplHandshake.onSuccessHandshake = v
        }

    fun requestConnectedUsers() = mImplHandshake
        .requestConnectedUsers()

    fun setCanSendFrames(
        canReceive: Boolean
    ) = mImplVideo.setCanSendFrames(
        canReceive
    )

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo
    ) = mAccepterStreamConfig.sendHandshakeSettings(
        mUserId,
        model
    )

    fun stopStreamingCamera() = mImplVideo
        .stopStreamingCamera()

}