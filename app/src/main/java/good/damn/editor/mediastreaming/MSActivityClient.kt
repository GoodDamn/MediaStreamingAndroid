package good.damn.editor.mediastreaming

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.stream.MSStreamAudioInput
import good.damn.editor.mediastreaming.camera.MSCamera
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnOpenCamera
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission
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

    private var mImageView: ImageView? = null
    private var mEditText: EditText? = null

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

            mImageView = ImageView(
                context
            ).apply {
                setBackgroundColor(
                    0xffff0000.toInt()
                )
                addView(this)
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
            }
        }

        else -> Unit
    }

    override fun onImageAvailable(
        reader: ImageReader?
    ) {
        Log.d(TAG, "onImageAvailable: $reader")
        reader?.apply {
            val image =  acquireLatestImage()
            val plane = image
                .planes[0]

            val buffer = plane.buffer

            val data = ByteArray(
                buffer.capacity()
            )

            buffer.get(data)

            val rowStride = plane.rowStride
            val pixelStride = plane.pixelStride
            Log.d(TAG, "onImageAvailable: SETUP: $rowStride $pixelStride")
            Log.d(TAG, "onImageAvailable: $width $height ${buffer.capacity()} ${image.planes.size}")

            BitmapFactory.decodeByteArray(
                data,
                0,
                data.size
            ).apply {
                Handler(
                    Looper.getMainLooper()
                ).post {
                    mImageView?.setImageBitmap(
                        this
                    )
                }
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