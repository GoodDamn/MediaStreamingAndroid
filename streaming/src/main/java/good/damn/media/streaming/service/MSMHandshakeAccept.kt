package good.damn.media.streaming.service

import good.damn.media.streaming.MSTypeDecoderSettings
import java.net.InetAddress

data class MSMHandshakeAccept(
    val settings: MSTypeDecoderSettings,
    val address: InetAddress,
    val userId: Int
)