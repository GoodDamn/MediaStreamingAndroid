package good.damn.media.streaming.service

import good.damn.media.streaming.MSTypeDecoderSettings
import java.net.InetAddress

interface MSListenerOnConnectUser {
    fun onConnectUser(
        userId: Int,
        settings: MSTypeDecoderSettings,
        fromIp: InetAddress
    )
}