package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.os.Bundle
import android.view.Gravity
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
import good.damn.editor.mediastreaming.MSEnvironmentAudio
import good.damn.editor.mediastreaming.MSEnvironmentVideo
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.extensions.hasPermissionMicrophone
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.editor.mediastreaming.views.MSViewStreamFrame
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.views.MSListenerOnChangeSurface
import good.damn.editor.mediastreaming.views.MSViewColor

class MSFragmentTestH264
: Fragment(),
MSListenerOnResultPermission,
MSListenerOnSelectCamera,
MSListenerOnChangeSurface {

    companion object {
        private const val TAG = "MSFragmentTestH264"
    }

    private var mEditTextHost: EditText? = null

    private val mServiceStreamWrapper = MSServiceStreamWrapper()
    private val mStreamCamera = MSEnvironmentVideo(
        mServiceStreamWrapper
    )

    private val mStreamAudio = MSEnvironmentAudio(
        mServiceStreamWrapper
    )

    private var mSurfaceReceive: Surface? = null

    override fun onPause() {
        super.onPause()
        mStreamCamera.stopReceiving()
    }

    override fun onDestroy() {
        super.onDestroy()

        mStreamAudio.releaseReceiving()
        mStreamCamera.releaseReceiving()

        context?.apply {
            mServiceStreamWrapper.destroy(
                this
            )
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        mServiceStreamWrapper.start(
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
                    mStreamAudio.stopReceiving()
                    return@setOnClickListener
                }

                val ip = mEditTextHost?.text?.toString()
                    ?: return@setOnClickListener

                val inet = ip.toInetAddress()

                text = "Stop receiving"
                mSurfaceReceive?.apply {
                    mStreamCamera.startReceiving(
                        this,
                        inet
                    )
                }

                mStreamAudio.startReceiving()
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

            MSViewColor(
                context
            ).apply {
                setBackgroundColor(
                    0xffff0000.toInt()
                )

                val s = (MSApp.width * 0.1f).toInt()
                layoutParams = FrameLayout.LayoutParams(
                    s, s
                ).apply {
                    gravity = Gravity.END or Gravity.TOP
                }

                setOnClickListener {
                    if (color == 0xffff0000.toInt()) {
                        // Audio disabled
                        (activity as? MSActivityMain)?.apply {
                            if (!hasPermissionMicrophone()) {
                                launcherPermission.launch(
                                    Manifest.permission.RECORD_AUDIO
                                )
                                return@setOnClickListener
                            }

                            setBackgroundColor(
                                0xff0000ff.toInt()
                            )

                            mEditTextHost?.text?.toString()?.apply {
                                mStreamAudio.startStreaming(
                                    this
                                )
                            }
                        }
                        return@setOnClickListener
                    }

                    setBackgroundColor(
                        0xffff0000.toInt()
                    )

                    mStreamAudio.stopStreaming()
                }

                it.addView(
                    this
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