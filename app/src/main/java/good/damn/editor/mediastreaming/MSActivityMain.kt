package good.damn.editor.mediastreaming

import android.Manifest
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.extensions.context
import good.damn.editor.mediastreaming.extensions.toast
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.editor.mediastreaming.views.MSListenerOnChangeSurface
import good.damn.editor.mediastreaming.views.MSViewFragmentTestH264
import good.damn.editor.mediastreaming.views.MSViewStreamFrame
import good.damn.editor.mediastreaming.views.dialogs.option.MSDialogOptionsH264
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.camera2.default
import good.damn.media.streaming.extensions.hasUpOsVersion
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

class MSActivityMain
: AppCompatActivity(),
MSListenerOnResultPermission,
MSListenerOnHandshakeSettings, MSListenerOnSelectCamera {

    companion object {
        private const val TAG = "MSActivityMain"
    }

    private var mView: MSViewFragmentTestH264? = null

    private val mLauncherPermission = MSPermission().apply {
        onResultPermission = this@MSActivityMain
    }

    private val mServiceStreamWrapper = MSServiceStreamWrapper()

    private val mStreamCamera = MSEnvironmentVideo(
        mServiceStreamWrapper
    )

    private val mEnvHandshake = MSEnvironmentHandshake().apply {
        onHandshakeSettings = this@MSActivityMain
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

    override fun onResume() {
        super.onResume()
        mServiceStreamWrapper.bind(
            context
        )
    }

    override fun onPause() {
        mStreamCamera.stopReceiving()
        mView?.layoutSurfaces?.removeAllViews()

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
        Log.d(TAG, "onCreate: ")

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy
                .Builder()
                .permitNetwork()
                .build()
        )

        mView = MSViewFragmentTestH264(
            context,
            this@MSActivityMain
        ) {
            val options = MSDialogOptionsH264(
                mOptionsHandshake
            )
            options.show(
                supportFragmentManager, "options"
            )
        }

        setContentView(
            mView
        )

        mLauncherPermission.register(
            this@MSActivityMain
        )

        if (!hasUpOsVersion(Build.VERSION_CODES.TIRAMISU)) {
            mLauncherPermission.launch(
                Manifest.permission.CAMERA
            )
            return
        }

        mLauncherPermission.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }


    override fun onResultPermissions(
        result: Map<String, Boolean>
    ) {
        for (entry in result) {
            if (!entry.value) {
                finish()
                return
            }
        }

        mServiceStreamWrapper.apply {
            startServiceStream(context)
            bind(context)
        }

        mEnvHandshake.startListeningSettings()
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

    override fun onSelectCamera(
        v: View?,
        cameraId: MSCameraModelID
    ) {
        context.toast("Wait")

        v?.apply {
            isEnabled = false
            postDelayed({
                isEnabled = true
            }, 1000)
        }

        val ip = mView?.editTextHost?.text?.toString()
            ?: return

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
                context.toast(
                    "No result"
                )
            }
            return
        }

        withContext(
            Dispatchers.Main
        ) {
            context.toast(
                "Connected"
            )
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
            mView?.layoutSurfaces?.apply {
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

        mView?.layoutSurfaces?.addView(
            streamFrame
        )
    }
}