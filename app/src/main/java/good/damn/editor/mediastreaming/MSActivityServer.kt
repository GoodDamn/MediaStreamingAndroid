package good.damn.editor.mediastreaming

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.MSRecordAudio
import good.damn.editor.mediastreaming.network.server.MSReceiverAudio
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFramePiece
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFramePiece
import good.damn.editor.mediastreaming.system.MSServiceHotspotCompat
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost
import good.damn.media.gles.GLViewTexture
import good.damn.media.gles.gl.textures.GLTexture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MSActivityServer
: AppCompatActivity(),
MSListenerOnGetHotspotHost,
 MSListenerOnReceiveFramePiece {

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
        8 + 12100 * 4,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverCameraFramePiece().apply {
            onReceiveFramePiece = this@MSActivityServer
        }
    )

    private var mViewTexture: GLViewTexture? = null
    private val mTexture = GLTexture(
        640,
        480
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

            GLViewTexture(
                context,
                mTexture
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

    override suspend fun onReceiveFramePiece(
        from: Int,
        to: Int,
        offsetPixels: Int,
        pixels: ByteArray
    ) {
        val toIndex = if (to - from > pixels.size)
            pixels.size
        else to

        var pixelsIndex = offsetPixels
        for (i in from until toIndex) {
            mTexture.buffer.put(
                i,
                pixels[pixelsIndex++]
            )
        }

        withContext(
            Dispatchers.Main
        ) {
            mViewTexture?.requestRender()
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