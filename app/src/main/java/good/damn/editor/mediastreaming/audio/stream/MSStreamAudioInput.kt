package good.damn.editor.mediastreaming.audio.stream

import good.damn.editor.mediastreaming.audio.MSRecordAudio
import good.damn.editor.mediastreaming.audio.MSListenerOnSamplesRecord
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.client.MSClientStreamUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.InetAddress

class MSStreamAudioInput
: MSStateable,
MSListenerOnSamplesRecord {

    companion object {
        private val TAG = MSStreamAudioInput::class.simpleName
    }

    var host: InetAddress
        get() = mClientAudioStream.host
        set(v) {
            mClientAudioStream.host = v
        }

    private val mAudioRecord = MSRecordAudio().apply {
        onSampleListener = this@MSStreamAudioInput
    }

    private val mClientAudioStream = MSClientStreamUDP(
        port = 5555,
        CoroutineScope(
            Dispatchers.IO
        )
    )

    override fun start(): Job {
        mAudioRecord.startRecording()
        return mClientAudioStream.start()
    }

    override fun stop() {
        mAudioRecord.stop()
        mClientAudioStream.stop()
    }

    override fun release() {
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