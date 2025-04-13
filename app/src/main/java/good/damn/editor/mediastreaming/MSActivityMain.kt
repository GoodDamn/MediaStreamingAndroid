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
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.MSStreamConstantsPacket
import good.damn.media.streaming.env.MSEnvironmentGroupStream
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.camera.models.MSMCameraId
import good.damn.media.streaming.extensions.camera2.default
import good.damn.media.streaming.extensions.hasUpOsVersion
import good.damn.media.streaming.extensions.setIntegerOnPosition
import good.damn.media.streaming.extensions.setShortOnPosition
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameUserDefault
import good.damn.media.streaming.service.impl.MSListenerOnConnectUser
import good.damn.media.streaming.service.impl.MSListenerOnSuccessHandshake
import good.damn.media.streaming.models.handshake.MSMHandshakeResult
import good.damn.media.streaming.models.handshake.MSMHandshakeAccept
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MSActivityMain
: AppCompatActivity(),
MSListenerOnResultPermission,
MSListenerOnSelectCamera, MSListenerOnSuccessHandshake, MSListenerOnConnectUser {

    companion object {
        private const val TAG = "MSActivityMain"
    }

    private var mView: MSViewFragmentTestH264? = null

    private val mLauncherPermission = MSPermission().apply {
        onResultPermission = this@MSActivityMain
    }

    private val mEnvGroupStream = MSEnvironmentGroupStream()

    private val mServiceStreamWrapper = MSServiceStreamWrapper()

    /*private val configFrame = byteArrayOf(
        0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 1, 103, 66, -64, 41, -115, 104, 10, 3, -38, 66, 18, 16, 18, 15, 8, -124, 106,
        0, 0, 0, 1, 104, -50, 1, -88, 53, -56
    )*/

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
        mServiceStreamWrapper.apply {
            onConnectUser = this@MSActivityMain
            //bind(context)
            requestConnectedUsers()

            if (isBound) {
                mEnvGroupStream.startReceivingFrames()
            }
        }
    }

    override fun onPause() {
        mEnvGroupStream.stop()
        mView?.layoutSurfaces?.removeAllViews()

        mServiceStreamWrapper.apply {
            onConnectUser = null
            //unbind(context)
        }

        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        mEnvGroupStream.release()
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

        mEnvGroupStream.startReceivingFrames()
    }


    override fun onSelectCamera(
        v: View?,
        cameraId: MSMCameraId
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

        if (mServiceStreamWrapper.isStreamingVideo) {
            mServiceStreamWrapper.stopStreamingVideo()
        }

        mServiceStreamWrapper.setCanSendFrames(false)

        mServiceStreamWrapper.sendHandshakeSettings(
            MSMHandshakeSendInfo(
                ip,
                mOptionsHandshake,
                cameraId,
                createDefaultMediaFormat(
                    mOptionsHandshake
                ).apply {
                    setInteger(
                        MediaFormat.KEY_ROTATION,
                        0
                    )
                }
            ),
            this@MSActivityMain
        )
    }

    override suspend fun onSuccessHandshake(
        result: MSMHandshakeResult?
    ) {
        if (result == null) {
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

        Log.d(TAG, "onSuccessHandshake: ")
        mServiceStreamWrapper.setCanSendFrames(true)
    }

    override fun onConnectUser(
        model: MSMHandshakeAccept
    ) {
        mEnvGroupStream.getUser(
            model.userId
        )?.apply {
            release()

            mEnvGroupStream.removeUser(
                model.userId
            )

            mView?.layoutSurfaces?.removeView(
                surfaceView
            )
        }

        val width = model.settings[
            MediaFormat.KEY_WIDTH
        ] ?: 640

        val height = model.settings[
            MediaFormat.KEY_HEIGHT
        ] ?: 480

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

        val user = MSReceiverCameraFrameUserDefault(
            streamFrame
        )
        
        val configPacket = ByteArray(
            MSStreamConstantsPacket.LEN_META
        ) + model.config


        configPacket.setShortOnPosition(
            model.config.size,
            MSStreamConstantsPacket.OFFSET_PACKET_SIZE,
        )

        Log.d(TAG, "onConnectUser: ${model.config.size} ${model.config.contentToString()}")

        user.setConfigFrame(
            configPacket
        )

        mEnvGroupStream.putUser(
            model.userId,
            user
        )

        streamFrame.onChangeSurface = MSListenerOnChangeSurface { surface ->
            user.startReceive(
                model.userId,
                surface,
                createDefaultMediaFormat(
                    model.settings
                ),
                model.address
            )
        }

        mView?.layoutSurfaces?.addView(
            streamFrame
        )
    }

}

private inline fun createDefaultMediaFormat(
    settings: MSTypeDecoderSettings
) = MediaFormat.createVideoFormat(
    MSCoder.MIMETYPE_CODEC,
    0,
    0
).apply {
    default()
    settings.forEach {
        setInteger(
            it.key,
            it.value
        )
    }
}