package good.damn.editor.mediastreaming.audio.stream

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import good.damn.editor.mediastreaming.audio.MSAudioRecord
import good.damn.editor.mediastreaming.audio.MSListenerOnSamplesRecord
import good.damn.editor.mediastreaming.network.MSClientAudio
import good.damn.editor.mediastreaming.stream.MSStream
import java.net.InetAddress

class MSStreamAudioInput
: MSStream,
MSListenerOnSamplesRecord {

    companion object {
        private val TAG = MSStreamAudioInput::class.simpleName
    }

    var host: InetAddress
        get() = mClientAudio.host
        set(v) {
            mClientAudio.host = v
        }

    private val mAudioRecord = MSAudioRecord().apply {
        onSampleListener = this@MSStreamAudioInput
    }

    private val mClientAudio = MSClientAudio()

    override fun start() {
        mClientAudio.stream()
        mAudioRecord.startRecording()
    }

    override fun stop() {
        mAudioRecord.stop()
        mClientAudio.stopStream()
    }

    override fun release() {
        mAudioRecord.release()
        mClientAudio.stopStream()
    }

    override fun onRecordSamples(
        samples: ByteArray,
        position: Int,
        len: Int
    ) {
        mClientAudio.sendToMediaServer(
            samples
        )
    }


}