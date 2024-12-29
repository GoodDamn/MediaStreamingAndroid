package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.avc.MSCameraAVC
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission

class MSFragmentTestH264
: Fragment(),
MSListenerOnResultPermission, MSListenerOnSelectCamera {

    private var managerCamera: MSManagerCamera? = null
    private var mCameraAvc: MSCameraAVC? = null

    private var mSurfaceDecoded: Surface? = null

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

        FrameLayout(
            context
        ).let {
            SurfaceView(
                context
            ).apply {
                it.addView(
                    this
                )
                post {
                    mSurfaceDecoded = holder.surface
                }
            }

            LinearLayout(
                context
            ).apply {

                orientation = LinearLayout
                    .VERTICAL

                Button(
                    context
                ).apply {

                    text = "Stop"

                    setOnClickListener {
                        mCameraAvc?.stop()
                    }

                    addView(
                        this,
                        -1,
                        -2
                    )
                }

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

    override fun onStop() {
        mCameraAvc?.stop()
        super.onStop()
    }

    override fun onDestroy() {
        mCameraAvc?.release()
        mCameraAvc = null

        mSurfaceDecoded?.release()
        mSurfaceDecoded = null

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
            mCameraAvc = MSCameraAVC(
                this
            )
        }
    }

    override fun onSelectCamera(
        cameraId: MSCameraModelID
    ) {
        val activity = activity as? MSActivityMain
            ?: return

        if (activity.hasPermissionCamera()) {
            if (mCameraAvc == null) {
                initCamera()
            }

            mCameraAvc?.apply {
                configure(
                    640,
                    480,
                    cameraId.characteristics,
                    mSurfaceDecoded!!
                )

                start(cameraId)
            }

            return
        }

        activity.launcherPermission.launch(
            Manifest.permission.CAMERA
        )
    }
}