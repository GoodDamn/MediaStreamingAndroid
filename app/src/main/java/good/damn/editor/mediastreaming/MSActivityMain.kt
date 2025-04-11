package good.damn.editor.mediastreaming

import android.Manifest
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
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
import good.damn.media.streaming.service.MSListenerOnSuccessHandshake
import good.damn.media.streaming.service.MSMHandshake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class MSActivityMain
: AppCompatActivity(),
MSListenerOnResultPermission,
MSListenerOnSelectCamera, MSListenerOnSuccessHandshake {

    companion object {
        private const val TAG = "MSActivityMain"
        private const val KEY_HOST_CONNECTED = "host"
    }

    private var mView: MSViewFragmentTestH264? = null

    private val mLauncherPermission = MSPermission().apply {
        onResultPermission = this@MSActivityMain
    }

    private val mServiceStreamWrapper = MSServiceStreamWrapper()
    private val mStreamCamera = MSEnvironmentVideoHandler()

    private var mTarget: MSMTarget? = null

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

        mTarget = MSMTarget(
            ip,
            cameraId
        )

        mServiceStreamWrapper.sendHandshakeSettings(
            ip,
            mOptionsHandshake,
            this@MSActivityMain
        )
    }

    private fun handshakeSurface(
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

        mStreamCamera.clearBuffer()
        Thread.sleep(1000)

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
                    MSCoder.MIME_TYPE_CODEC,
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

    override suspend fun onSuccessHandshake(
        result: MSMHandshake
    ) {
        withContext(
            Dispatchers.Main
        ) {
            context.toast(
                "Connected"
            )
        }

        if (mServiceStreamWrapper.isStreamingVideo) {
            mServiceStreamWrapper.stopStreamingVideo()
        }

        val width = mOptionsHandshake[
            MediaFormat.KEY_WIDTH
        ] ?: 640

        val height = mOptionsHandshake[
            MediaFormat.KEY_HEIGHT
        ] ?: 480

        val target = mTarget
            ?: return


        mServiceStreamWrapper.startStreamingVideo(
            target.camera,
            target.ip,
            MediaFormat.createVideoFormat(
                MSCoder.MIME_TYPE_CODEC,
                width,
                height
            ).apply {
                default()

                mOptionsHandshake.forEach {
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
}

private data class MSMTarget(
    val ip: String,
    val camera: MSCameraModelID
)