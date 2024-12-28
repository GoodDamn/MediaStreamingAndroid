package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.camera.MSCamera
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.avc.MSCameraAVC
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission

class MSFragmentTestH264
: Fragment(),
MSListenerOnResultPermission {

    private var managerCamera: MSManagerCamera? = null
    private var mCamera: MSCamera? = null
    private var mCameraAvc: MSCameraAVC? = null

    private var mSurface: Surface? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LinearLayout(
        context
    ).apply {

        orientation = LinearLayout
            .VERTICAL

        Button(
            context
        ).apply {

            text = "Open camera"

            setOnClickListener {
                onClickBtnOpenCamera(it)
            }

            addView(
                this,
                -1,
                -2
            )
        }


        SurfaceView(
            context
        ).apply {
            addView(
                this
            )
            post {
                mSurface = holder.surface
            }
        }

    }

    override fun onStop() {
        mCameraAvc?.release()
        mCamera?.release()
        mSurface?.release()
        super.onStop()
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

    private inline fun onClickBtnOpenCamera(
        v: View
    ) {
        if (managerCamera == null) {
            if (!v.context.hasPermissionCamera()) {
                (activity as? MSActivityMain)
                    ?.launcherPermission
                    ?.launch(
                        Manifest.permission.CAMERA
                    )
                return
            }

            initCamera()
        }

        mCamera?.apply {
            val camera = camera
                ?: return

            openCameraStream(
                camera
            )
        }
    }


    private inline fun initCamera() {
        val context = context
            ?: return

        managerCamera = MSManagerCamera(
            context
        ).apply {
            mCamera = MSCamera(
                this
            ).apply {
                val cameraId = getCameraIds()
                    .firstOrNull()
                    ?: return@apply

                mCameraAvc = MSCameraAVC(
                    640,
                    480,
                    cameraId.characteristics
                ).apply {

                    configure(
                        mSurface!!
                    )

                    surfaces = arrayListOf(
                        createEncodeSurface()
                    )

                    start()
                }

                openCameraStream(
                    cameraId
                )
            }
        }
    }
}