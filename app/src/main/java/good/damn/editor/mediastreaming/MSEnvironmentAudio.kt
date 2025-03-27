package good.damn.editor.mediastreaming

import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.audio.stream.MSStreamAudioInput
import good.damn.media.streaming.network.server.udp.MSReceiverAudio
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

class MSEnvironmentAudio(
    private val mServiceStreamWrapper: MSServiceStreamWrapper
) {

    val isReceiving: Boolean
        get() = mServerAudio.isRunning

    val isStreamingAudio: Boolean
        get() = mServiceStreamWrapper
            .serviceConnectionStream
            .binder
            ?.isStreamingAudio ?: false

    private val mReceiverAudio = MSReceiverAudio()
    private val mServerAudio = MSServerUDP(
        MSStreamConstants.PORT_AUDIO,
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

    fun startStreaming(
        host: String
    ) = mServiceStreamWrapper
        .serviceConnectionStream
        .binder
        ?.startStreamingAudio(
            host
        )

    fun stopStreaming() = mServiceStreamWrapper
        .serviceConnectionStream
        .binder
        ?.stopStreamingAudio()

}