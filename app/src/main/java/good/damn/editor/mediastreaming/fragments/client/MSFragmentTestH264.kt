package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.MSEnvironmentHandshake
import good.damn.editor.mediastreaming.MSEnvironmentVideo
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.extensions.toast
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.editor.mediastreaming.views.MSViewStreamFrame
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.views.MSListenerOnChangeSurface
import good.damn.editor.mediastreaming.views.dialogs.option.MSDialogOptionsH264
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.extensions.camera2.default
import good.damn.media.streaming.extensions.hasUpOsVersion
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

class MSFragmentTestH264
: Fragment(),
MSListenerOnResultPermission,
MSListenerOnSelectCamera,
MSListenerOnHandshakeSettings {

    companion object {
        private const val TAG = "MSFragmentTestH264"
    }

    private var mEditTextHost: EditText? = null
    private var mLayoutSurfaces: LinearLayout? = null

    private val mServiceStreamWrapper = MSServiceStreamWrapper()

    private val mStreamCamera = MSEnvironmentVideo(
        mServiceStreamWrapper
    )

    private val mEnvHandshake = MSEnvironmentHandshake().apply {
        onHandshakeSettings = this@MSFragmentTestH264
    }

    private val mOptionsHandshake = hashMapOf(
        MediaFormat.KEY_WIDTH to 640,
        MediaFormat.KEY_HEIGHT to 480,
        MediaFormat.KEY_ROTATION to 90,
        MediaFormat.KEY_BIT_RATE to 1024 * 8,
        MediaFormat.KEY_CAPTURE_RATE to 1,
        MediaFormat.KEY_FRAME_RATE to 1,
        MediaFormat.KEY_I_FRAME_INTERVAL to 1
    )

    private var mIsNeedToReceive = false

    override fun onResume() {
        super.onResume()
        mServiceStreamWrapper.bind(
            context
        )
    }

    override fun onPause() {
        mStreamCamera.stopReceiving()
        mServiceStreamWrapper.unbind(
            context
        )
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        mEnvHandshake.release()
        mStreamCamera.stopStreamingCamera()
        mStreamCamera.releaseReceiving()

        mServiceStreamWrapper.destroy(
            context
        )
        super.onDestroy()
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        (activity as? MSActivityMain)?.launcherPermission?.apply {
            if (!hasUpOsVersion(Build.VERSION_CODES.TIRAMISU)) {
                launch(
                    Manifest.permission.CAMERA
                )
                return
            }

            launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )

        }
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
                if (mIsNeedToReceive) {
                    text = "Start listening peers"
                    mEnvHandshake.stop()
                    mIsNeedToReceive = false
                    return@setOnClickListener
                }

                mIsNeedToReceive = true
                text = "Stop listening peers"
                mEnvHandshake.startListeningSettings()
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

            LinearLayout(
                context
            ).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(0)
                mLayoutSurfaces = this

                layoutParams = FrameLayout.LayoutParams(
                    -2,
                    -2
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

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

                Button(
                    context
                ).apply {
                    text = "options"

                    setOnClickListener {
                        onClickSetupStreaming()
                    }

                    addView(
                        this,
                        -2,
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


    override fun onResultPermissions(
        result: Map<String, Boolean>
    ) {
        for (entry in result) {
            if (!entry.value) {
                activity?.finish()
                return
            }
        }

        mServiceStreamWrapper.apply {
            startServiceStream(context)
            bind(context)
        }
    }

    override fun onSelectCamera(
        v: View?,
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

        context?.toast("Wait")

        v?.apply {
            isEnabled = false
            postDelayed({
                isEnabled = true
            }, 1000)
        }

        CoroutineScope(
            Dispatchers.IO
        ).launch {
            sendHandshake(
                ip,
                cameraId,
                mOptionsHandshake
            )
        }

    }


    override suspend fun onHandshakeSettings(
        settings: MSTypeDecoderSettings,
        fromIp: InetAddress
    ) {
        val width = settings[
            MediaFormat.KEY_WIDTH
        ] ?: 640

        val height = settings[
            MediaFormat.KEY_HEIGHT
        ] ?: 480

        Log.d(TAG, "onHandshakeSettings: ")
        withContext(
            Dispatchers.Main
        ) {
            handshakeSurface(
                fromIp,
                width,
                height,
                settings
            )
        }
    }

    private inline fun onClickSetupStreaming() {
        val options = MSDialogOptionsH264(
            mOptionsHandshake
        )
        options.show(
            childFragmentManager, "options"
        )
    }

    private suspend inline fun sendHandshake(
        ip: String,
        cameraId: MSCameraModelID,
        settings: MSTypeDecoderSettings
    ) {
        val result = mEnvHandshake.sendHandshakeSettings(
            ip,
            settings
        )

        if (!result) {
            withContext(
                Dispatchers.Main
            ) {
                context?.toast(
                    "No result"
                )
            }
            return
        }

        if (mStreamCamera.isStreamingVideo) {
            mStreamCamera.stopStreamingCamera()
        }

        val width = settings[
            MediaFormat.KEY_HEIGHT
        ] ?: 640

        val height = settings[
            MediaFormat.KEY_HEIGHT
        ] ?: 480

        mStreamCamera.startStreamingCamera(
            cameraId,
            ip,
            MediaFormat.createVideoFormat(
                MSCoder.TYPE_AVC,
                width,
                height
            ).apply {
                default()

                settings.forEach {
                    setInteger(
                        it.key,
                        it.value
                    )
                }

                setInteger(
                    MediaFormat.KEY_ROTATION,
                    0
                )
            }
        )
    }

    private inline fun handshakeSurface(
        fromIp: InetAddress,
        width: Int,
        height: Int,
        settings: MSTypeDecoderSettings
    ) {
        if (mStreamCamera.isReceiving) {
            mLayoutSurfaces?.apply {
                removeViewAt(
                    childCount - 1
                )
            }
            mStreamCamera.stopReceiving()
        }

        val streamFrame = MSViewStreamFrame(
            context
        ).apply {
            val w = MSApp.width * 0.4f
            layoutParams = LinearLayout.LayoutParams(
                w.toInt(),
                ((width.toFloat() / height) * w).toInt()
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }
        
        streamFrame.onChangeSurface = MSListenerOnChangeSurface {
                surface ->
            mStreamCamera.startReceiving(
                surface,
                MediaFormat.createVideoFormat(
                    MSCoder.TYPE_AVC,
                    width,
                    height
                ).apply {
                    default()
                    settings.forEach {
                        setInteger(
                            it.key,
                            it.value
                        )
                    }
                },
                fromIp
            )
        }

        mLayoutSurfaces?.addView(
            streamFrame
        )
    }

}