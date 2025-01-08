package good.damn.editor.mediastreaming.camera

import good.damn.editor.mediastreaming.network.client.MSClientStreamUDPChunk
import good.damn.editor.mediastreaming.network.client.MSModelChunkUDP
import kotlinx.coroutines.CoroutineScope
import java.net.InetAddress

class MSStreamSubscriberUDP(
    scope: CoroutineScope
): MSStreamSubscriber {

    private val mClient = MSClientStreamUDPChunk(
        5556,
        scope
    )

    var host: InetAddress
        get() = mClient.host
        set(v) {
            mClient.host = v
        }

    fun start() = mClient.start()

    override fun onGetPacket(
        data: ByteArray
    ) {
        mClient.sendToStream(
            MSModelChunkUDP(
                data,
                0,
                data.size
            )
        )
    }
}