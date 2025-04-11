package good.damn.media.streaming.service

import android.content.Context
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriber
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.extensions.camera2.default
import good.damn.media.streaming.network.client.MSClientUDP
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import good.damn.media.streaming.network.server.tcp.MSServerTCP
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class MSServiceStreamImpl {

    companion object {
        private const val TAG = "MSServiceStreamImpl"
        const val EXTRA_CAMERA_ID_LOGICAL = "l"
        const val EXTRA_CAMERA_ID_PHYSICAL = "P"
        const val EXTRA_VIDEO_WIDTH = "W"
        const val EXTRA_VIDEO_HEIGHT = "H"
        const val EXTRA_HOST = "h"
    }

    private val mImplVideo = MSServiceStreamImplVideo()
    private val mImplHandshake = MSServiceStreamImplHandshake()

    private val mBinder = MSServiceStreamBinder(
        mImplVideo,
        mImplHandshake
    )

    fun startCommand(
        context: Context
    ) {
        Log.d(TAG, "startCommand: ")

        mImplVideo.startCommand(
            context
        )

        mImplHandshake.startCommand()
        mImplHandshake.startListeningSettings()
    }

    fun getBinder() = mBinder
    
    fun destroy() {
        Log.d(TAG, "destroy: ")
        mImplHandshake.destroy()
        mImplVideo.destroy()
    }
}