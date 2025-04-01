package good.damn.editor.mediastreaming.system.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.audio.stream.MSStreamAudioInput
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriber
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.client.MSClientUDP
import good.damn.media.streaming.network.server.udp.MSReceiverAudio
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.math.log

class MSServiceStream
: Service(), MSStreamSubscriber {

    companion object {
        private val TAG = MSServiceStream::class.simpleName
        const val EXTRA_CAMERA_ID_LOGICAL = "l"
        const val EXTRA_CAMERA_ID_PHYSICAL = "P"
        const val EXTRA_VIDEO_WIDTH = "W"
        const val EXTRA_VIDEO_HEIGHT = "H"
        const val EXTRA_HOST = "h"
    }

    private var mStreamCamera: MSStreamCameraInput? = null
    private var mClientStreamCamera: MSClientUDP? = null
    private var mServerRestorePackets: MSServerUDP? = null

    private var mBinder: MSServiceStreamBinder? = null

    private var mThread: HandlerThread? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Log.d(TAG, "onStartCommand: ")

        mThread = HandlerThread(
            "communicationThread"
        ).apply {
            start()
        }

        mClientStreamCamera = MSClientUDP(
            MSStreamConstants.PORT_VIDEO
        )

        mStreamCamera = MSStreamCameraInput(
            MSManagerCamera(
                baseContext
            )
        ).apply {
            subscribers = arrayListOf(
                this@MSServiceStream
            )
        }

        mServerRestorePackets = MSServerUDP(
            MSStreamConstants.PORT_VIDEO_RESTORE_REQUEST,
            64,
            CoroutineScope(
                Dispatchers.IO
            ),
            MSReceiverCameraFrameRestore().apply {
                bufferizer = mStreamCamera!!.bufferizer
            }
        )

        mBinder = MSServiceStreamBinder(
            mClientStreamCamera,
            mStreamCamera,
            mServerRestorePackets,
            Handler(
                mThread!!.looper
            )
        )

        return START_NOT_STICKY
    }

    override fun onBind(
        intent: Intent?
    ) = mBinder

    override fun onDestroy() {
        mStreamCamera?.stop()
        mServerRestorePackets?.stop()

        mThread?.quitSafely()
        mThread = null

        mStreamCamera?.release()
        mClientStreamCamera?.release()
        mServerRestorePackets?.release()
        Log.d(TAG, "onDestroy: ")
    }

    override fun onGetPacket(
        data: ByteArray
    ) {
        mClientStreamCamera?.sendToStream(
            data
        )
    }
}