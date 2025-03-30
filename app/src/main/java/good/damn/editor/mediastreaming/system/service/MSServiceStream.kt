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
import good.damn.media.streaming.camera.MSStreamSubscriberUDP
import good.damn.media.streaming.network.server.udp.MSReceiverAudio
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSServiceStream
: Service() {

    companion object {
        private val TAG = MSServiceStream::class.simpleName
        const val EXTRA_CAMERA_ID_LOGICAL = "l"
        const val EXTRA_CAMERA_ID_PHYSICAL = "P"
        const val EXTRA_VIDEO_WIDTH = "W"
        const val EXTRA_VIDEO_HEIGHT = "H"
        const val EXTRA_HOST = "h"
    }

    private var managerCamera: MSManagerCamera? = null
    private var mSubscriber: MSStreamSubscriberUDP? = null
    private var mStreamCamera: MSStreamCameraInput? = null
    private var mStreamAudio: MSStreamAudioInput? = null

    private var mServerRestorePackets: MSServerUDP? = null
    private var mReceiverCameraFrameRestore: MSReceiverCameraFrameRestore? = null

    private lateinit var mBinder: MSServiceStreamBinder

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

        managerCamera = MSManagerCamera(
            applicationContext
        )

        mSubscriber = MSStreamSubscriberUDP(
            MSStreamConstants.PORT_VIDEO
        )

        mStreamCamera = MSStreamCameraInput(
            managerCamera!!
        ).apply {
            subscribers = arrayListOf(
                mSubscriber!!
            )
        }

        mReceiverCameraFrameRestore = MSReceiverCameraFrameRestore().apply {
            bufferizer = mStreamCamera!!.bufferizer
        }


        mServerRestorePackets = MSServerUDP(
            MSStreamConstants.PORT_VIDEO_RESTORE_REQUEST,
            64,
            CoroutineScope(
                Dispatchers.IO
            ),
            mReceiverCameraFrameRestore!!
        )

        //mStreamAudio = MSStreamAudioInput()

        mBinder = MSServiceStreamBinder(
            managerCamera!!,
            mSubscriber!!,
            mStreamCamera!!,
            mServerRestorePackets!!,
            mReceiverCameraFrameRestore!!,
            Handler(
                mThread!!.looper
            )
        )

        return START_STICKY
    }

    override fun onBind(
        intent: Intent?
    ): IBinder {
        Log.d(TAG, "onBind: $intent")
        return mBinder
    }

    override fun onUnbind(
        intent: Intent?
    ): Boolean {
        Log.d(TAG, "onUnbind: ")
        return true
    }

    override fun onRebind(
        intent: Intent?
    ) {
        Log.d(TAG, "onRebind: ")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mThread?.quitSafely()
        mThread = null

        mBinder.release()

    }

}