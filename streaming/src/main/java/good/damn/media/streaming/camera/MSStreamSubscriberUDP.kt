package good.damn.media.streaming.camera

import good.damn.media.streaming.network.client.MSClientUDP
import kotlinx.coroutines.CoroutineScope
import java.net.InetAddress

class MSStreamSubscriberUDP(
    port: Int
): MSStreamSubscriber {

    private val mClient = MSClientUDP(
        port
    )

    var host: InetAddress
        get() = mClient.host
        set(v) {
            mClient.host = v
        }

    fun release() = mClient.release()

    override fun onGetPacket(
        data: ByteArray
    ) {
        mClient.sendToStream(
            data
        )
    }
}