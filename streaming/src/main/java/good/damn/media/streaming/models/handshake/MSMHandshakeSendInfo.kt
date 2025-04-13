package good.damn.media.streaming.models.handshake

import android.media.MediaFormat
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.models.MSMCameraId
import java.net.InetAddress

data class MSMHandshakeSendInfo(
    val host: String,
    val settings: MSTypeDecoderSettings,
    val cameraId: MSMCameraId,
    val format: MediaFormat,
    var config: ByteArray? = null
)