package good.damn.media.streaming.service

import android.os.Binder
import good.damn.media.streaming.models.MSMStream
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo
import good.damn.media.streaming.service.impl.MSAccepterStreamConfigHandshake
import good.damn.media.streaming.service.impl.MSListenerOnConnectUser
import good.damn.media.streaming.service.impl.MSListenerOnSuccessHandshake
import good.damn.media.streaming.service.impl.MSServiceStreamImplHandshake
import good.damn.media.streaming.service.impl.MSServiceStreamImplVideo

class MSServiceStreamBinder(
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
        get() = mImplVideo.onSuccessHandshake
        set(v) {
            mImplVideo.onSuccessHandshake = v
        }

    fun requestConnectedUsers() = mImplHandshake
        .requestConnectedUsers()

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo
    ) = mAccepterStreamConfig.sendHandshakeSettings(
        model
    )

    fun startStreamingCamera(
        stream: MSMStream
    ) = mImplVideo.startStreamingCamera(
        stream
    )

    fun stopStreamingCamera() = mImplVideo
        .stopStreamingCamera()

}