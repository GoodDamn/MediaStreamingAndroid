package good.damn.media.streaming.audio.stream

import good.damn.media.streaming.audio.MSListenerOnSamplesRecord
import good.damn.media.streaming.audio.MSRecordAudio
import good.damn.media.streaming.network.MSStateable
import good.damn.media.streaming.network.client.MSClientStreamUDPAudio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    var roomId: Byte
        get() = mClientAudioStream.roomId
        set(v) {
            mClientAudioStream.roomId = v
        }

    var userId: Byte
        get() = mClientAudioStream.userId
        set(v) {
            mClientAudioStream.userId = v
        }

    private val mAudioRecord = good.damn.media.streaming.audio.MSRecordAudio().apply {
        onSampleListener = this@MSStreamAudioInput
    }

    private val mClientAudioStream = MSClientStreamUDPAudio(
        port = 5555,
        CoroutineScope(
            Dispatchers.IO
        ),
        2048
    )

    override fun start() {
        mAudioRecord.startRecording()
        mClientAudioStream.start()
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