package good.damn.media.streaming.models.handshake

import good.damn.media.streaming.MSTypeDecoderSettings
import java.net.InetAddress

data class MSMHandshakeAccept(
    val settings: MSTypeDecoderSettings,
    val address: InetAddress,
    val userId: Int
) {
    override fun toString() = "$userId: ${address.hostAddress}"
}