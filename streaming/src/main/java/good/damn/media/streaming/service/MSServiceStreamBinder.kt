package good.damn.media.streaming.service

import android.media.MediaFormat
import android.os.Binder
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.toInetAddress

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
        host: String,
        settings: MSTypeDecoderSettings
    ) = mImplHandshake.sendHandshakeSettings(
        host,
        settings
    )

    fun startStreamingCamera(
        modelID: MSCameraModelID,
        mediaFormat: MediaFormat,
        host: String
    ) = mImplVideo.startStreamingCamera(
        userId,
        modelID,
        mediaFormat,
        host
    )

    fun stopStreamingCamera() = mImplVideo
        .stopStreamingCamera()

}