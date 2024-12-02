package good.damn.editor.mediastreaming

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.MSAudioRecord
import good.damn.editor.mediastreaming.network.server.MSServerAudio
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerServerOnReceiveSamples
import good.damn.editor.mediastreaming.system.MSServiceHotspotCompat
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost

class MSActivityServer
: AppCompatActivity(),
MSListenerServerOnReceiveSamples, MSListenerOnGetHotspotHost {

    private val mServer = MSServerAudio().apply {
        onReceiveSamples = this@MSActivityServer
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
                AudioFormat.CHANNEL_OUT_MONO
            ).build(),
        MSAudioRecord.DEFAULT_BUFFER_SIZE,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )

    private var mTextViewIp: TextView? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = this

        LinearLayout(
            context
        ).apply {
            orientation = LinearLayout
                .VERTICAL

            mTextViewIp = TextView(
                context
            ).apply {

                addView(
                    this,
                    -2,
                    -2
                )
            }

            Button(
                context
            ).apply {

                text = "Start server"

                setOnClickListener {
                    onClickBtnStartServer(this)
                }

                addView(
                    this,
                    -1,
                    -2
                )
            }

            Button(
                context
            ).apply {

                setOnClickListener {
                    onClickBtnStopServer(this)
                }

                text = "Stop server"

                addView(
                    this,
                    -1,
                    -2
                )
            }

            setContentView(
                this
            )
        }

        MSServiceHotspotCompat(
            context
        ).apply {
            delegate = this@MSActivityServer
            start()
        }

    }


    private inline fun onClickBtnStartServer(
        btn: Button
    ) {
        mServer.start(
            port = 5555
        )
    }

    private inline fun onClickBtnStopServer(
        btn: Button
    ) {
        mServer.stop()
    }

    override fun onReceiveSamples(
        samples: ByteArray,
        offset: Int,
        len: Int
    ) = mAudioTrack.run {
        write(
            samples,
            offset,
            len
        )

        play()
    }

    override fun onGetHotspotIP(
        addressList: String
    ) {
        mTextViewIp?.text = "Host: $addressList\n\nPort: 5555"
    }

}