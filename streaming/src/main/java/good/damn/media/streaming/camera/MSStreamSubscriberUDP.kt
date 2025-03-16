package good.damn.media.streaming.camera

import good.damn.media.streaming.network.client.MSClientStreamUDPChunk
import kotlinx.coroutines.CoroutineScope
import java.net.InetAddress

class MSStreamSubscriberUDP(
    port: Int,
    scope: CoroutineScope
): MSStreamSubscriber {

    private val mClient = MSClientStreamUDPChunk(
        port,
        scope
    )

    var host: InetAddress
        get() = mClient.host
        set(v) {
            mClient.host = v
        }

    fun start() = mClient.start()

    fun stop() = mClient.stop()

    fun release() = mClient.release()

    override fun onGetPacket(
        data: ByteArray
    ) {
        mClient.sendToStream(
            data
        )
    }
}