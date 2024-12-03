package good.damn.editor.mediastreaming.network.server

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import good.damn.editor.mediastreaming.audio.MSAudioRecord
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData

class MSReceiverAudio
: MSListenerOnReceiveData {

    private val mAudioTrack = AudioTrack(
        AudioAttributes.Builder()
            .setLegacyStreamType(
                AudioManager.STREAM_MUSIC
            ).setContentType(
                AudioAttributes.CONTENT_TYPE_SPEECH
            ).build(),
        AudioFormat.Builder()
            .setSampleRate(
                MSAudioRecord.DEFAULT_SAMPLE_RATE
            ).setEncoding(
                MSAudioRecord.DEFAULT_ENCODING
            ).setChannelMask(
                AudioFormat.CHANNEL_OUT_MONO
            ).build(),
        MSAudioRecord.DEFAULT_BUFFER_SIZE,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )

    override fun onReceiveData(
        data: ByteArray
    ) = mAudioTrack.run {
        write(
            data,
            0,
            data.size
        )
        play()
    }
}