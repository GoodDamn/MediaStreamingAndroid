package good.damn.media.streaming.service.impl

import good.damn.media.streaming.camera.MSStreamCameraInputConfig
import good.damn.media.streaming.camera.MSStreamCameraInputFrame
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo
import java.nio.ByteBuffer

class MSAccepterStreamConfigHandshake(
    private val implHandshake: MSServiceStreamImplHandshake
): MSStreamCameraInputConfig {

    private var model: MSMHandshakeSendInfo? = null

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo
    ) {
        this.model = model
    }

    override fun onGetCameraConfigStream(
        data: ByteBuffer,
        offset: Int,
        len: Int
    ) {
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