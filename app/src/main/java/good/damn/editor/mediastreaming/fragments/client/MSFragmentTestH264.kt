package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.os.Bundle
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.MSStreamCameraInput
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectResolution
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectResolution
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

class MSFragmentTestH264
: Fragment(),
MSListenerOnResultPermission, MSListenerOnSelectCamera, MSListenerOnSelectResolution {

    private var managerCamera: MSManagerCamera? = null
    private var mCameraStream: MSStreamCameraInput? = null

    private var mLayoutContent: FrameLayout? = null
    private var mEditTextHost: EditText? = null

    private val mResolutions = arrayOf(
        Size(176, 144),
        Size(320,240),
        Size(640, 480),
        Size(1280, 720),
        Size(1920, 1080)
    )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = context
            ?: return

        managerCamera = MSManagerCamera(
            context
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LinearLayout(
        context
    ).apply {

        orientation = LinearLayout
            .VERTICAL

        EditText(
            context
        ).apply {

            mEditTextHost = this

            setText(
                "127.0.0.1"
            )

            addView(
                this,
                -1,
                -2
            )
        }

        FrameLayout(
            context
        ).let {
            mLayoutContent = it
            LinearLayout(
                context
            ).apply {

                orientation = LinearLayout
                    .VERTICAL

                managerCamera?.getCameraIds()?.forEach {
                    addView(
                        Button(
                            context
                        ).apply {
                            text = "${it.logical}_${it.physical ?: ""}"
                            setOnClickListener(
                                MSClickOnSelectCamera(
                                    it
                                ).apply {
                                    onSelectCamera = this@MSFragmentTestH264
                                }
                            )
                        },
                        (0.15f * MSApp.width).toInt(),
                        -2
                    )
                }


                it.addView(
                    this,
                    -1,
                    -2
                )
            }

            addView(
                it
            )
        }

    }

    override fun onDestroy() {
        mCameraStream?.release()
        mCameraStream = null
        super.onDestroy()
    }

    override fun onResultPermission(
        permission: String,
        result: Boolean
    ) {
        if (!result) {
            return
        }

        when (permission) {
            Manifest.permission.CAMERA -> {
                initCamera()
            }
        }

    }

    private inline fun initCamera() {
        managerCamera?.apply {
            mCameraStream = MSStreamCameraInput(
                this,
                CoroutineScope(
                    Dispatchers.IO
                )
            )
        }
    }

    override fun onSelectCamera(
        cameraId: MSCameraModelID
    ) {
        val activity = activity as? MSActivityMain
            ?: return

        if (activity.hasPermissionCamera()) {
            if (mCameraStream == null) {
                initCamera()
            }

            mCameraStream?.apply {
                if (isRunning) {
                    stop()

                    mLayoutContent?.apply {
                        removeViewAt(
                            childCount - 1
                        )
                    }
                }
            }

            LinearLayout(
                context
            ).apply {

                orientation = LinearLayout
                    .VERTICAL

                setBackgroundColor(0)

                mResolutions.forEach {
                    Button(
                        context
                    ).apply {

                        text = "${it.width}x${it.height}"

                        setOnClickListener(
                            MSClickOnSelectResolution(
                                it,
                                cameraId
                            ).apply {
                                onSelectResolution = this@MSFragmentTestH264
                            }
                        )

                        addView(
                            this,
                            -2,
                            -2
                        )
                    }
                }

                layoutParams = FrameLayout.LayoutParams(
                    -2,
                    -2
                ).apply {
                    gravity = Gravity.END or Gravity.TOP
                }

                mLayoutContent?.addView(
                    this
                )
            }

            return
        }

        activity.launcherPermission.launch(
            Manifest.permission.CAMERA
        )
    }

    override fun onSelectResolution(
        resolution: Size,
        cameraId: MSCameraModelID
    ) = mCameraStream?.run {
        if (isRunning) {
            stop()
        }

        mEditTextHost?.apply {
            host = InetAddress.getByName(
                text.toString()
            )
        }

        start(
            cameraId,
            640,
            480
        )

    } ?: Unit
}