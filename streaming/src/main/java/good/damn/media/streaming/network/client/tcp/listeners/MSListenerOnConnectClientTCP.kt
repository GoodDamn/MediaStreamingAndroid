package good.damn.media.streaming.network.client.tcp.listeners

import java.io.InputStream
import java.io.OutputStream

interface MSListenerOnConnectClientTCP {
    fun onConnect(
        inp: InputStream,
        out: OutputStream
    )
}