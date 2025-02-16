package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectResolution
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectResolution
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.views.MSViewStreamFrame
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.camera.service.MSServiceStreamWrapper
import good.damn.media.streaming.network.server.MSReceiverCameraFrame
import good.damn.media.streaming.network.server.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

class MSFragmentTestH264
: Fragment(),
MSListenerOnResultPermission,
MSListenerOnSelectCamera,
MSListenerOnSelectResolution {

    companion object {
        private const val TAG = "MSFragmentTestH264"
        private val RESOLUTION = Size(
            640,
            480
        )
    }
    
    private var mLayoutContent: FrameLayout? = null
    private var mEditTextHost: EditText? = null

    private val mReceiverFrame = MSReceiverCameraFrame()
    private val mServiceStreamWrapper = MSServiceStreamWrapper()

    private val mServerUDP = MSServerUDP(
        5556,
        MSStreamCameraInput.PACKET_MAX_SIZE + MSUtilsAvc.LEN_META,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverFrame
    )

    override fun onResume() {
        super.onResume()
        mReceiverFrame.start()
        mServerUDP.start()
    }

    override fun onPause() {
        super.onPause()
        mReceiverFrame.stop()
        mServerUDP.stop()
    }

    override fun onDestroy() {
        mReceiverFrame.release()
        mServerUDP.release()

        Log.d(TAG, "onDestroy: ")
        context?.apply {
            mServiceStreamWrapper.destroy(
                this
            )
        }
        super.onDestroy()
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
            setBackgroundColor(0)
            mLayoutContent = it
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

        MSViewStreamFrame(
            context,
            mReceiverFrame,
            mServerUDP,
            MediaFormat.createVideoFormat(
                MSCoder.TYPE_AVC,
                RESOLUTION.width,
                RESOLUTION.height
            ).apply {
                setInteger(
                    MediaFormat.KEY_ROTATION,
                    90
                )
            }
        ).apply {

            layoutParams = ViewGroup.LayoutParams(
                MSApp.width,
                (RESOLUTION.width.toFloat() / RESOLUTION.height * MSApp.width).toInt()
            )

            mLayoutContent?.addView(
                this,
                0
            )
        }
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
                initServiceStream()
            }
        }

    }

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

        if (!mServiceStreamWrapper.isStarted) {
            initServiceStream()
        }

        mServiceStreamWrapper.unbind(
            activity
        )

        LinearLayout(
            context
        ).apply {

            orientation = LinearLayout
                .VERTICAL

            setBackgroundColor(0)

            Button(
                context
            ).apply {

                isAllCaps = false
                text = RESOLUTION.toString()

                setOnClickListener(
                    MSClickOnSelectResolution(
                        RESOLUTION,
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
    }

    override fun onSelectResolution(
        resolution: Size,
        cameraId: MSCameraModelID
    ) = mEditTextHost?.run {
        mServiceStreamWrapper.bind(
            resolution.width,
            resolution.height,
            cameraId,
            text.toString(),
            context
        )
    } ?: Unit

    private inline fun initServiceStream() {
        // start service
        context?.apply {
            mServiceStreamWrapper.start(
                this
            )
        }
    }
}