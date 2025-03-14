package good.damn.editor.mediastreaming.system.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriberUDP
import good.damn.media.streaming.camera.models.MSCameraModelID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

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

    private lateinit var managerCamera: MSManagerCamera
    private lateinit var mSubscriber: MSStreamSubscriberUDP
    private lateinit var mStreamCamera: MSStreamCameraInput

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
            managerCamera
        ).apply {
            subscribers = arrayListOf(
                mSubscriber
            )
        }

        return START_STICKY
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        Log.d(TAG, "onBind: $intent")

        intent ?: return null

        val logical = intent.getStringExtra(
            EXTRA_CAMERA_ID_LOGICAL
        ) ?: return null

        val physical = intent.getStringExtra(
            EXTRA_CAMERA_ID_PHYSICAL
        )

        val width = intent.getIntExtra(
            EXTRA_VIDEO_WIDTH, 0
        )

        val height = intent.getIntExtra(
            EXTRA_VIDEO_HEIGHT, 0
        )

        if (width == 0 || height == 0) {
            return null
        }

        mSubscriber.host = InetAddress.getByName(
            intent.getStringExtra(
                EXTRA_HOST
            )
        )

        mSubscriber.start()

        mStreamCamera.start(
            MSCameraModelID(
                logical,
                physical,
                characteristics = managerCamera.getCharacteristics(
                    physical ?: logical
                )
            ),
            width,
            height
        )

        return null
    }

    override fun onUnbind(
        intent: Intent?
    ): Boolean {
        Log.d(TAG, "onUnbind: ")
        mSubscriber.stop()
        mStreamCamera.stop()
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        mSubscriber.stop()
        mSubscriber.release()

        mStreamCamera.stop()
        mStreamCamera.release()

        super.onDestroy()
    }

}