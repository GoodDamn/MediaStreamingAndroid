package good.damn.editor.mediastreaming.network.client.tcp

import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

abstract class MSClientBaseTCP {
    var host: InetSocketAddress? = null
    protected var mSocket: Socket? = null

    fun release() {
        mSocket?.close()
        mSocket = null
    }
}