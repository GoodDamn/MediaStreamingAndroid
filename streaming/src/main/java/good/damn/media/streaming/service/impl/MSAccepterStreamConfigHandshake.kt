package good.damn.media.streaming.service.impl

import android.util.Log
import good.damn.media.streaming.camera.MSStreamCameraInputConfig
import good.damn.media.streaming.camera.MSStreamCameraInputFrame
import good.damn.media.streaming.models.MSMStream
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo
import java.nio.ByteBuffer

class MSAccepterStreamConfigHandshake(
    private val implHandshake: MSServiceStreamImplHandshake,
    private val implVideo: MSServiceStreamImplVideo
): MSStreamCameraInputConfig {

    private var model: MSMHandshakeSendInfo? = null

    fun sendHandshakeSettings(
        userId: Int,
        model: MSMHandshakeSendInfo
    ) {
        this.model = model
        implVideo.startStreamingCamera(
            MSMStream(
                userId,
                model.host,
                model.cameraId,
                model.format
            )
        )
    }

    override fun onGetCameraConfigStream(
        data: ByteBuffer,
        offset: Int,
        len: Int
    ) {
        Log.d("ssss", "onGetCameraConfigStream: ")
        model?.config = ByteArray(
            len
        ).apply {
            data.get(this)
        }

        implHandshake.sendHandshakeSettings(
            model
        )
    }

}