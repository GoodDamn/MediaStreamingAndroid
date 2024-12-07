package good.damn.editor.mediastreaming.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.MSStreamCameraInput
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnUpdateCameraFrame
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.extensions.hasPermissionMicrophone
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission
import good.damn.media.gles.GLViewTexture
import good.damn.media.gles.gl.textures.GLTextureBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

class MSFragmentClient
: Fragment(),
MSListenerOnResultPermission,
MSListenerOnUpdateCameraFrame {

    companion object {
        private val TAG = MSFragmentClient::class.simpleName
        const val PREVIEW_WIDTH = 360
        const val PREVIEW_HEIGHT = 240
    }

    private var managerCamera: MSManagerCamera? = null

    private var mStreamInputAudio: MSStreamAudioInput? = null
    private var mStreamInputCamera: MSStreamCameraInput? = null

    private var mLauncherPermission: MSPermission? = null

    private var mViewTexture: GLViewTexture? = null
    private val mTexture = GLTextureBuffer(
        PREVIEW_WIDTH,
        PREVIEW_HEIGHT
    )
    private var mEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context
            ?: return null

        managerCamera = MSManagerCamera(
            context
        )

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

            FrameLayout(
                context
            ).let { surface ->
                mViewTexture = GLViewTexture(
                    context,
                    mTexture
                ).apply {
                    surface.addView(
                        this,
                        -1,
                        -1
                    )
                }

                managerCamera?.apply {
                    val btnHeight = 100
                    var yPos = 0
                    cameraIds.forEach { cameraId ->
                        Button(
                            context
                        ).apply {
                            text = cameraId
                            setTextSize(
                                TypedValue.COMPLEX_UNIT_PX,
                                35f
                            )
                            setOnClickListener {
                                onClickBtnCamera(
                                    this,
                                    cameraId
                                )
                            }
                            layoutParams = FrameLayout.LayoutParams(
                                -2,
                                btnHeight
                            ).apply {
                                topMargin = yPos
                            }
                            yPos += (btnHeight * 1.001f).toInt()
                            surface.addView(
                                this
                            )
                        }
                    }
                }

                addView(
                    surface,
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
    ) {
        if (!result) {
            return
        }

        when (
            permission
        ) {
            Manifest.permission.RECORD_AUDIO -> {
                mStreamInputAudio = MSStreamAudioInput()
            }

            Manifest.permission.CAMERA -> {
                initCamera()
            }
        }
    }

    private inline fun onClickBtnVideoCall(
        btn: Button
    ) {
        if (mStreamInputCamera == null) {
            if (!btn.context.hasPermissionCamera()) {
                mLauncherPermission?.launch(
                    Manifest.permission.CAMERA
                )
                return
            }
            initCamera()
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
            if (!btn.context.hasPermissionMicrophone()) {
                mLauncherPermission?.launch(
                    Manifest.permission.RECORD_AUDIO
                )
                return
            }
            mStreamInputAudio = MSStreamAudioInput()
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

    private inline fun onClickBtnCamera(
        btn: Button,
        cameraId: String
    ) {
        mStreamInputCamera?.apply {
            stop()
            this.cameraId = cameraId
            start()
        }
    }

    private inline fun initCamera() {

        val manager = managerCamera
            ?: return

        mStreamInputCamera = MSStreamCameraInput(
            manager,
            CoroutineScope(
                Dispatchers.IO
            ),
            mTexture
        ).apply {
            onUpdateCameraFrame = this@MSFragmentClient
            mViewTexture?.rotationShade = rotation
        }
    }
}