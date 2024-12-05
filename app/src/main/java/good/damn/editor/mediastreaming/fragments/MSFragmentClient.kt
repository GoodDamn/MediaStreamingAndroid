package good.damn.editor.mediastreaming.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.audio.stream.MSStreamAudioInput
import good.damn.editor.mediastreaming.camera.MSStreamCameraInput
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnUpdateCameraFrame
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission
import good.damn.media.gles.GLViewTexture
import good.damn.media.gles.gl.textures.GLTexture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

class MSFragmentClient
: Fragment(),
MSListenerOnResultPermission,
MSListenerOnUpdateCameraFrame {

    companion object {
        private val TAG = MSFragmentClient::class.simpleName
        private const val CAMERA_WIDTH = 640
        private const val CAMERA_HEIGHT = 480
    }

    private var mStreamInputAudio: MSStreamAudioInput? = null
    private var mStreamInputCamera: MSStreamCameraInput? = null

    private var mLauncherPermission: MSPermission? = null

    private var mViewTexture: GLViewTexture? = null
    private val mTexture = GLTexture(
        CAMERA_WIDTH,
        CAMERA_HEIGHT
    )
    private var mEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context
            ?: return null

        mEditText = EditText(
            context
        ).apply {
            hint = "Host"
        }

        val root = LinearLayout(
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
                context,
                mTexture
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

        }

        mLauncherPermission = (activity as? MSActivityMain)
            ?.launcherPermission

        return root
    }

    override fun onStop() {
        mStreamInputAudio?.release()
        mStreamInputCamera?.release()
        super.onStop()
    }


    override fun onUpdateFrame() {
        mViewTexture?.requestRender()
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
                requireContext(),
                CoroutineScope(
                    Dispatchers.IO
                ),
                mTexture
            ).apply {
                onUpdateCameraFrame = this@MSFragmentClient
                mViewTexture?.rotationShade = rotation
            }
        }

        else -> Unit
    }

    private inline fun onClickBtnVideoCall(
        btn: Button
    ) {
        if (mStreamInputCamera == null) {
            mLauncherPermission?.launch(
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
            mLauncherPermission?.launch(
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