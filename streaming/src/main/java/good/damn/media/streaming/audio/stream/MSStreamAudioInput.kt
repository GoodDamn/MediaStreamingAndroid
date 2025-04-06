package good.damn.media.streaming.audio.stream

import android.os.Handler
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.audio.MSListenerOnSamplesRecord
import good.damn.media.streaming.audio.MSRecordAudio
import good.damn.media.streaming.network.client.MSClientUDP
import java.net.InetAddress

class MSStreamAudioInput
: MSListenerOnSamplesRecord {

    companion object {
        private val TAG = MSStreamAudioInput::class.simpleName
    }

    val isRunning: Boolean
        get() = mAudioRecord.isRunning

    private val mAudioRecord = MSRecordAudio().apply {
        onSampleListener = this@MSStreamAudioInput
    }

    private val mClientAudio = MSClientUDP(
        MSStreamConstants.PORT_MEDIA,
    )

    fun start(
        host: InetAddress,
        handler: Handler
    ) {
        mAudioRecord.start(
            handler
        )
        mClientAudio.host = host
    }

    fun stop() {
        mAudioRecord.stop()
    }

    fun release() {
        mAudioRecord.release()
        mClientAudio.release()
    }

    override fun onRecordSamples(
        samples: ByteArray,
        position: Int,
        len: Int
    ) {
        mClientAudio.sendToStream(
            samples
        )
    }


}