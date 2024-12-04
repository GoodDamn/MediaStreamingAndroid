package good.damn.editor.mediastreaming

import android.Manifest
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.stream.MSStreamAudioInput
import good.damn.editor.mediastreaming.camera.MSCamera
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission
import good.damn.media.gles.GLViewTexture
import java.net.InetAddress

class MSActivityClient
: AppCompatActivity(),
MSListenerOnResultPermission,
ImageReader.OnImageAvailableListener {

    companion object {
        private val TAG = MSActivityClient::class.simpleName
    }

    private val mLauncherPermission = MSPermission().apply {
        onResultPermission = this@MSActivityClient
    }

    private var mStreamAudio: MSStreamAudioInput? = null
    private var mCamera: MSCamera? = null

    private var mViewTexture: GLViewTexture? = null
    private var mEditText: EditText? = null

    private val mHandlerMain = Handler(
        Looper.getMainLooper()
    )

    private val mReader = ImageReader.newInstance(
        800,
        640,
        ImageFormat.JPEG,
        1
    )


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
                    mReader.height,
                    mReader.width
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
        mStreamAudio?.release()
        mCamera?.release()
        super.onStop()
    }


    override fun onResultPermission(
        permission: String,
        result: Boolean
    ) = when (
        permission
    ) {
        Manifest.permission.RECORD_AUDIO -> {
            mStreamAudio = MSStreamAudioInput()
        }

        Manifest.permission.CAMERA -> {
            mCamera = MSCamera(
                this
            ).apply {
                mReader.setOnImageAvailableListener(
                    this@MSActivityClient,
                    Handler(
                        thread.looper
                    )
                )
                Log.d(TAG, "onResultPermission: $rotation")
                mViewTexture?.rotationShade = rotation
            }
        }

        else -> Unit
    }

    override fun onImageAvailable(
        reader: ImageReader?
    ) {
        reader?.apply {

            val image = acquireLatestImage()

            val buffer = image
                .planes[0]
                .buffer

            val data = ByteArray(
                buffer.capacity()
            )

            buffer.get(
                data
            )

            mViewTexture?.bitmap = BitmapFactory.decodeByteArray(
                data,
                0,
                data.size
            )

            mHandlerMain.post {
                mViewTexture?.requestRender()
            }

            image.close()
        }
    }

    private inline fun onClickBtnVideoCall(
        btn: Button
    ) {
        Log.d(TAG, "onClickBtnVideoCall: $mCamera ${mReader.surface}")
        if (mCamera == null) {
            mLauncherPermission.launch(
                Manifest.permission.CAMERA
            )
            return
        }
        mCamera?.openCameraStream(
            listOf(
                mReader.surface
            )
        )
    }

    private inline fun onClickBtnCall(
        btn: Button
    ) {
        Log.d(TAG, "onClickBtnCall: $mStreamAudio")
        if (mStreamAudio == null) {
            mLauncherPermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
            return
        }

        mStreamAudio?.apply {
            host = InetAddress.getByName(
                mEditText?.text?.toString()
            )
            start()
        }
    }

    private inline fun onClickBtnDecline(
        btn: Button
    ) {
        mStreamAudio?.stop()
    }

}