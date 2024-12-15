package good.damn.editor.mediastreaming.network.server.accepters

import android.content.Intent
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnAcceptClient
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress

class MSAccepterConnectRoom
: MSListenerOnAcceptClient {

    override fun onAcceptClient(
        fromAddress: InetAddress,
        inp: InputStream,
        out: OutputStream
    ) {

    }

}