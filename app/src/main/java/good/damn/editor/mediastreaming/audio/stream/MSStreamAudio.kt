package good.damn.editor.mediastreaming.audio.stream

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import good.damn.editor.mediastreaming.audio.MSAudioRecord
import good.damn.editor.mediastreaming.audio.MSListenerOnSamplesRecord
import good.damn.editor.mediastreaming.stream.MSStream

class MSStreamAudio
: MSStream,
MSListenerOnSamplesRecord {

    private val mAudioRecord = MSAudioRecord().apply {
        onSampleListener = this@MSStreamAudio
    }

    private val mAudioTrack = AudioTrack(
        AudioAttributes.Builder()
            .setLegacyStreamType(
                AudioManager.STREAM_VOICE_CALL
            ).setContentType(
                AudioAttributes.CONTENT_TYPE_SPEECH
            ).build(),
        AudioFormat.Builder()
            .setSampleRate(
                MSAudioRecord.DEFAULT_SAMPLE_RATE
            ).setEncoding(
                MSAudioRecord.DEFAULT_ENCODING
            ).setChannelMask(
                MSAudioRecord.DEFAULT_CHANNEL
            ).build(),
        MSAudioRecord.DEFAULT_BUFFER_SIZE,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )


    override fun start() {
        mAudioRecord.startRecording()
    }

    override fun stop() {
        mAudioRecord.stop()
    }

    override fun release() {
        mAudioRecord.release()
    }

    override fun onRecordSamples(
        samples: ByteArray,
        position: Int,
        len: Int
    ) {
        mAudioTrack.write(
            samples,
            0,
            len
        )
        mAudioTrack.play()
    }


}