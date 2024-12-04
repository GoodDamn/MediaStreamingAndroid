package good.damn.editor.mediastreaming

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.stream.MSStreamAudioInput
import good.damn.editor.mediastreaming.camera.MSStreamCameraInput
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameBitmap
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameData
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission
import good.damn.media.gles.GLViewTexture
import java.net.InetAddress

class MSActivityClient
: AppCompatActivity(),
MSListenerOnResultPermission, MSListenerOnGetCameraFrameData, MSListenerOnGetCameraFrameBitmap {

    companion object {
        private val TAG = MSActivityClient::class.simpleName
        private const val CAMERA_WIDTH = 480
        private const val CAMERA_HEIGHT = 360
    }

    private val mLauncherPermission = MSPermission().apply {
        onResultPermission = this@MSActivityClient
    }

    private var mStreamInputAudio: MSStreamAudioInput? = null
    private var mStreamInputCamera: MSStreamCameraInput? = null

    private var mViewTexture: GLViewTexture? = null
    private var mEditText: EditText? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = this

        mEditText = EditText(
            context
        ).apply {
            hint = "Host"
        }

        LinearLayout(
            context
        ).apply {

            orientation = LinearLayout
                .VERTICAL

            addView(
                mEditText,
                -1,
                -2
            )

            Button(
                context
            ).apply {
                text = "Call"

                setOnClickListener {
                    onClickBtnCall(this)
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
                text = "Video Call"

                setOnClickListener {
                    onClickBtnVideoCall(this)
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
                text = "Decline"

                setOnClickListener {
                    onClickBtnDecline(this)
                }

                addView(
                    this,
                    -1,
                    -2
                )
            }

            mViewTexture = GLViewTexture(
                context
            ).apply {
                addView(
                    this,
                    -1,
                    -1
                )
            }

            layoutParams = FrameLayout.LayoutParams(
                -1,
                -1
            )

            setContentView(
                this
            )
        }

        mLauncherPermission.apply {
            register(context)
            launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    override fun onStop() {
        mStreamInputAudio?.release()
        mStreamInputCamera?.release()
        super.onStop()
    }

    override fun onGetFrame(
        data: ByteArray
    ) {
        val bitmap = BitmapFactory.decodeByteArray(
            data,
            0,
            data.size
        )

        mViewTexture?.apply {
            MSApp.ui {
                this.bitmap = bitmap
                requestRender()
            }
        }
    }

    override fun onResultPermission(
        permission: String,
        result: Boolean
    ) = when (
        permission
    ) {
        Manifest.permission.RECORD_AUDIO -> {
            mStreamInputAudio = MSStreamAudioInput()
        }

        Manifest.permission.CAMERA -> {
            mStreamInputCamera = MSStreamCameraInput(
                CAMERA_WIDTH,
                CAMERA_HEIGHT,
                this
            ).apply {
                onGetCameraFrameBitmap = this@MSActivityClient
                mViewTexture?.rotationShade = rotation
            }
        }

        else -> Unit
    }

    override fun onGetFrameBitmap(
        bitmap: Bitmap
    ) {
        mViewTexture?.apply {
            MSApp.ui {
                this.bitmap = bitmap
                requestRender()
            }
        }
    }

    private inline fun onClickBtnVideoCall(
        btn: Button
    ) {
        if (mStreamInputCamera == null) {
            mLauncherPermission.launch(
                Manifest.permission.CAMERA
            )
            return
        }
        mStreamInputCamera?.apply {
            host = InetAddress.getByName(
                mEditText?.text?.toString()
            )
            start()
        }
    }

    private inline fun onClickBtnCall(
        btn: Button
    ) {
        Log.d(TAG, "onClickBtnCall: $mStreamInputAudio")
        if (mStreamInputAudio == null) {
            mLauncherPermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
            return
        }

        mStreamInputAudio?.apply {
            host = InetAddress.getByName(
                mEditText?.text?.toString()
            )
            start()
        }
    }

    private inline fun onClickBtnDecline(
        btn: Button
    ) {
        mStreamInputAudio?.stop()
        mStreamInputCamera?.stop()
    }

}