package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
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
MSListenerOnResultPermission, TextureView.SurfaceTextureListener {

    private var managerCamera: MSManagerCamera? = null
    private var mCamera: MSCamera? = null
    private var mCameraAvc: MSCameraAVC? = null

    private var mSurfaceTexture: Surface? = null

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


        TextureView(
            context
        ).apply {

            surfaceTextureListener = this@MSFragmentTestH264

            addView(
                this
            )
        }

    }

    override fun onStop() {
        mCameraAvc?.release()
        mCamera?.release()
        mSurfaceTexture?.release()
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

        mCamera?.openCameraStream()
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
                cameraId = getCameraIds().lastOrNull()
                mCameraAvc = MSCameraAVC(
                    640,
                    480,
                    this,
                    mSurfaceTexture!!
                ).apply {
                    start()
                }

                openCameraStream()
            }
        }
    }

    override fun onSurfaceTextureAvailable(
        surface: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        surface.setDefaultBufferSize(
            480, 640
        )
        mSurfaceTexture = Surface(
            surface
        )
    }

    override fun onSurfaceTextureSizeChanged(
        surface: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        mSurfaceTexture = Surface(
            surface
        )
    }

    override fun onSurfaceTextureDestroyed(
        surface: SurfaceTexture
    ): Boolean {
        mSurfaceTexture = null
        return true
    }

    override fun onSurfaceTextureUpdated(
        surface: SurfaceTexture
    ) {}
}