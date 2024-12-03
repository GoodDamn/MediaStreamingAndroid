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
import good.damn.editor.mediastreaming.network.server.MSReceiverAudio
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.system.MSServiceHotspotCompat
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSActivityServer
: AppCompatActivity(),
MSListenerOnGetHotspotHost {

    private val mServerAudio = MSServerUDP(
        5555,
        MSAudioRecord.DEFAULT_BUFFER_SIZE,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverAudio()
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
        mServerAudio.start()
    }

    private inline fun onClickBtnStopServer(
        btn: Button
    ) {
        mServerAudio.stop()
    }

    override fun onGetHotspotIP(
        addressList: String
    ) {
        mTextViewIp?.text = "Host: $addressList\n\nPort: 5555"
    }

    override fun onStop() {
        mServerAudio.release()
        super.onStop()
    }

}