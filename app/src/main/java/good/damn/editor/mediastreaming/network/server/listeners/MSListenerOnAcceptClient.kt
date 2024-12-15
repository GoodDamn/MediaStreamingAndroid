package good.damn.editor.mediastreaming.network.server.listeners

import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress

interface MSListenerOnAcceptClient {
    fun onAcceptClient(
        fromAddress: InetAddress,
        inp: InputStream,
        out: OutputStream
    )
}