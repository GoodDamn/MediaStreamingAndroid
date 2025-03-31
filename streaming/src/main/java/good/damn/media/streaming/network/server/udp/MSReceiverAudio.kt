package good.damn.media.streaming.network.server.udp

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import good.damn.media.streaming.audio.MSRecordAudio
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveNetworkData
import java.net.InetAddress

class MSReceiverAudio
: MSListenerOnReceiveNetworkData {

    companion object {
        private const val TAG = "MSReceiverAudio"
        const val BUFFER_SIZE = 1024
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

    override suspend fun onReceiveNetworkData(
        data: ByteArray,
        src: InetAddress
    ) = mAudioTrack.run {
        write(
            data,
            0,
            BUFFER_SIZE
        )

        if (mAudioTrack.state == AudioTrack.STATE_INITIALIZED) {
            play()
        }
    }

    fun stop() {
        mAudioTrack.stop()
    }

    fun release() {
        mAudioTrack.release()
    }
}