package good.damn.media.streaming.service

import android.os.Binder
import good.damn.media.streaming.models.MSMStream
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo
import good.damn.media.streaming.service.impl.MSListenerOnConnectUser
import good.damn.media.streaming.service.impl.MSListenerOnSuccessHandshake
import good.damn.media.streaming.service.impl.MSServiceStreamImplHandshake
import good.damn.media.streaming.service.impl.MSServiceStreamImplVideo

class MSServiceStreamBinder(
    private val mImplVideo: MSServiceStreamImplVideo,
    private val mImplHandshake: MSServiceStreamImplHandshake
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

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo
    ) = mImplHandshake.sendHandshakeSettings(
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