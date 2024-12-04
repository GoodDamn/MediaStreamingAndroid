package good.damn.editor.mediastreaming

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.MSRecordAudio
import good.damn.editor.mediastreaming.network.server.MSReceiverAudio
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFrame
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFrame
import good.damn.editor.mediastreaming.system.MSServiceHotspotCompat
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost
import good.damn.media.gles.GLViewTexture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSActivityServer
: AppCompatActivity(),
MSListenerOnGetHotspotHost, MSListenerOnReceiveFrame {

    private val mServerAudio = MSServerUDP(
        5555,
        MSRecordAudio.DEFAULT_BUFFER_SIZE,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverAudio()
    )

    private val mServerFrame = MSServerUDP(
        5556,
        1024 * 65,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverCameraFrame().apply {
            onReceiveFrame = this@MSActivityServer
        }
    )

    private var mViewTexture: GLViewTexture? = null
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

            GLViewTexture(
                context
            ).apply {
                mViewTexture = this
                addView(
                    this
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

    override fun onGetHotspotIP(
        addressList: String
    ) {
        mTextViewIp?.text = "Host: $addressList\n\nPort: 5555"
    }

    override fun onStop() {
        mServerAudio.release()
        mServerFrame.release()
        super.onStop()
    }

    override fun onReceiveFrame(
        frame: Bitmap
    ) {
        mViewTexture?.apply {
            bitmap = frame
            requestRender()
        }
    }

    private inline fun onClickBtnStartServer(
        btn: Button
    ) {
        mServerAudio.start()
        mServerFrame.start()
    }

    private inline fun onClickBtnStopServer(
        btn: Button
    ) {
        mServerAudio.stop()
        mServerFrame.stop()
    }

}