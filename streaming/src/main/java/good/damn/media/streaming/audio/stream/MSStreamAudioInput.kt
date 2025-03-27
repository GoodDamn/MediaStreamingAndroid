package good.damn.media.streaming.audio.stream

import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.audio.MSListenerOnSamplesRecord
import good.damn.media.streaming.audio.MSRecordAudio
import good.damn.media.streaming.network.client.MSClientStreamUDPAudio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

class MSStreamAudioInput
: MSListenerOnSamplesRecord {

    companion object {
        private val TAG = MSStreamAudioInput::class.simpleName
    }

    val isRunning: Boolean
        get() = mClientAudioStream.isStreamRunning

    private val mAudioRecord = MSRecordAudio(
        CoroutineScope(
            Dispatchers.IO
        )
    ).apply {
        onSampleListener = this@MSStreamAudioInput
    }

    private val mClientAudioStream = MSClientStreamUDPAudio(
        MSStreamConstants.PORT_AUDIO,
        CoroutineScope(
            Dispatchers.IO
        ),
        1024
    )

    fun start(
        host: InetAddress
    ) {
        mAudioRecord.startRecording()
        mClientAudioStream.apply {
            this.host = host
            start()
        }
    }

    fun stop() {
        mAudioRecord.stop()
        mClientAudioStream.stop()
    }

    fun release() {
        mAudioRecord.release()
        mClientAudioStream.release()
    }

    override fun onRecordSamples(
        samples: ByteArray,
        position: Int,
        len: Int
    ) {
        samples.forEach {
            mClientAudioStream.sendToStream(
                it
            )
        }
    }


}