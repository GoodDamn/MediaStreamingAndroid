package good.damn.editor.mediastreaming.network.server

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import good.damn.editor.mediastreaming.audio.MSRecordAudio
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData

class MSReceiverAudio
: MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverAudio"
        const val BUFFER_SIZE = 2048
    }

    private val mAudioTrack = AudioTrack(
        AudioAttributes.Builder()
            .setLegacyStreamType(
                AudioManager.STREAM_MUSIC
            ).setContentType(
                AudioAttributes.CONTENT_TYPE_SPEECH
            ).build(),
        AudioFormat.Builder()
            .setSampleRate(
                MSRecordAudio.DEFAULT_SAMPLE_RATE
            ).setEncoding(
                MSRecordAudio.DEFAULT_ENCODING
            ).setChannelMask(
                AudioFormat.CHANNEL_OUT_MONO
            ).build(),
        BUFFER_SIZE,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )

    override suspend fun onReceiveData(
        data: ByteArray
    ) = mAudioTrack.run {
        write(
            data,
            2,
            BUFFER_SIZE - 2
        )
        play()
    }
}