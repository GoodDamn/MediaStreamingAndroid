package good.damn.editor.mediastreaming

import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.network.server.udp.MSReceiverAudio
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSEnvironmentAudio {

    val isReceiving: Boolean
        get() = mServerAudio.isRunning

    private val mReceiverAudio = MSReceiverAudio()
    private var mServerAudio = MSServerUDP(
        MSStreamConstants.PORT_MEDIA,
        1024,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverAudio
    )

    fun releaseReceiving() {
        mReceiverAudio.release()
        mServerAudio.release()
    }

    fun stopReceiving() {
        mReceiverAudio.stop()
        mServerAudio.stop()
    }

    fun startReceiving() {
        mServerAudio.start()
    }

}