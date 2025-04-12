package good.damn.media.streaming.models.handshake

import good.damn.media.streaming.MSTypeDecoderSettings
import java.net.InetAddress

data class MSMHandshakeSendInfo(
    val host: InetAddress,
    val settings: MSTypeDecoderSettings
)