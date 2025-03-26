package good.damn.editor.mediastreaming.system.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriberUDP
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
    private var mServerRestorePackets: MSServerUDP? = null
    private var mReceiverCameraFrameRestore: MSReceiverCameraFrameRestore? = null

    private lateinit var mBinder: MSServiceStreamBinder

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Log.d(TAG, "onStartCommand: ")

        managerCamera = MSManagerCamera(
            applicationContext
        )

        mSubscriber = MSStreamSubscriberUDP(
            6666,
            CoroutineScope(
                Dispatchers.IO
            )
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
            5555,
            64,
            CoroutineScope(
                Dispatchers.IO
            ),
            mReceiverCameraFrameRestore!!
        )

        mBinder = MSServiceStreamBinder(
            managerCamera!!,
            mSubscriber!!,
            mStreamCamera!!,
            mServerRestorePackets!!,
            mReceiverCameraFrameRestore!!
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

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        mSubscriber?.apply {
            stop()
            release()
        }

        mStreamCamera?.apply {
            stop()
            release()
        }

        mServerRestorePackets?.apply {
            stop()
            release()
        }

        super.onDestroy()
    }

}