package good.damn.editor.mediastreaming.fragments

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.audio.MSRecordAudio
import good.damn.editor.mediastreaming.extensions.integer
import good.damn.editor.mediastreaming.network.server.MSReceiverAudio
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFramePiece
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFramePiece
import good.damn.editor.mediastreaming.system.MSServiceHotspotCompat
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost
import good.damn.media.gles.GLViewTexture
import good.damn.media.gles.gl.textures.GLTexture
import good.damn.media.gles.gl.textures.GLTextureBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MSFragmentServer
: Fragment(),
MSListenerOnGetHotspotHost,
MSListenerOnReceiveFramePiece {

    companion object {
        private val TAG = MSFragmentServer::class.simpleName
    }

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
        60000,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverCameraFramePiece().apply {
            onReceiveFramePiece = this@MSFragmentServer
        }
    )

    private var mViewTexture: GLViewTexture? = null
    private val mTexture = GLTextureBitmap(
        MSFragmentClient.PREVIEW_WIDTH,
        MSFragmentClient.PREVIEW_HEIGHT
    )
    private var mTextViewIp: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context
            ?: return null

        val root = LinearLayout(
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

        }

        MSServiceHotspotCompat(
            context
        ).apply {
            delegate = this@MSFragmentServer
            start()
        }

        return root
    }


    override fun onGetHotspotIP(
        addressList: String
    ) {
        MSApp.ui {
            mTextViewIp?.text = "Host: $addressList\n\n" +
                "Port: 5555 (Audio)\n" +
                "Port: 5556 (Camera)"
        }
    }

    override fun onStop() {
        mServerAudio.release()
        mServerFrame.release()
        super.onStop()
    }

    override suspend fun onReceiveFramePiece(
        pixels: ByteArray
    ) {
        val bitmapSize = pixels.integer(0)
        Log.d(TAG, "onReceiveFramePiece: $bitmapSize ${pixels.size}")

        mTexture.bitmap = BitmapFactory.decodeByteArray(
            pixels,
            4,
            bitmapSize
        )

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