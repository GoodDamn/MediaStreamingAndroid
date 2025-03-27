package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.MSEnvironmentVideoConf
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.views.MSViewStreamFrame
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.views.MSListenerOnChangeSurface
import java.net.InetAddress

class MSFragmentTestH264
: Fragment(),
MSListenerOnResultPermission,
MSListenerOnSelectCamera,
MSListenerOnChangeSurface {

    companion object {
        private const val TAG = "MSFragmentTestH264"
    }

    private var mEditTextHost: EditText? = null

    private val mStreamCamera = MSEnvironmentVideoConf()

    /*private val mReceiverAudio = MSReceiverAudio()
    private val mServerAudio = MSServerUDP(
        MSStreamConstants.PORT_AUDIO,
        1024,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverAudio
    )*/


    private var mSurfaceReceive: Surface? = null

    override fun onPause() {
        super.onPause()
        mStreamCamera.stopReceiving()
    }

    override fun onDestroy() {
        super.onDestroy()
        mStreamCamera.releaseReceiving(
            context
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        mStreamCamera.configureService(
            requireActivity().applicationContext
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

        Button(
            context
        ).apply {

            text = "Start receiving"

            setOnClickListener {
                if (mStreamCamera.isReceiving) {
                    text = "Start receiving"
                    mStreamCamera.stopReceiving()
                    return@setOnClickListener
                }

                val ip = mEditTextHost?.text?.toString()
                    ?: return@setOnClickListener

                val inet = InetAddress.getByName(ip)

                text = "Stop receiving"
                mSurfaceReceive?.apply {
                    mStreamCamera.startReceiving(
                        this,
                        inet
                    )
                }
            }

            addView(
                this,
                -1,
                -2
            )
        }

        FrameLayout(
            context
        ).let {
            setBackgroundColor(0)

            MSViewStreamFrame(
                context
            ).apply {

                onChangeSurface = this@MSFragmentTestH264

                layoutParams = ViewGroup.LayoutParams(
                    MSApp.width,
                    (mStreamCamera.resolution.width.toFloat() / mStreamCamera.resolution.height * MSApp.width).toInt()
                )
                it.addView(
                    this
                )
            }

            LinearLayout(
                context
            ).apply {

                orientation = LinearLayout
                    .VERTICAL

                MSManagerCamera(
                    context
                ).getCameraIds().forEach {
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


    override fun onResultPermission(
        permission: String,
        result: Boolean
    ) = Unit

    override fun onSelectCamera(
        cameraId: MSCameraModelID
    ) {
        val activity = activity as? MSActivityMain
            ?: return

        if (!activity.hasPermissionCamera()) {
            activity.launcherPermission.launch(
                Manifest.permission.CAMERA
            )
            return
        }

        val ip = mEditTextHost?.text?.toString()
            ?: return


        mStreamCamera.apply {
            if (isStreamingVideo) {
                stopStreamingCamera()
            }

            startStreamingCamera(
                cameraId.logical,
                cameraId.physical,
                ip
            )
        }

    }


    override fun onChangeSurface(
        surface: Surface
    ) {
        mSurfaceReceive = surface
    }

}