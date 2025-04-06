package good.damn.editor.mediastreaming

import good.damn.editor.mediastreaming.system.service.MSCameraServiceConnection
import good.damn.editor.mediastreaming.system.service.MSServiceStreamBinder
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.audio.stream.MSStreamAudioInput
import good.damn.media.streaming.network.server.udp.MSReceiverAudio
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

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