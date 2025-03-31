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

        return START_NOT_STICKY
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        Log.d(TAG, "onBind: $intent")
        return startStream(intent)
    }
    
    override fun onUnbind(
        intent: Intent?
    ): Boolean {
        Log.d(TAG, "onUnbind: ")
        mStreamCamera?.stop()
        mServerRestorePackets?.stop()

        return true
    }

    override fun onRebind(
        intent: Intent?
    ) {
        Log.d(TAG, "onRebind: $intent")
        startStream(intent)
    }
    
    override fun onDestroy() {
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

    private inline fun startStream(
        intent: Intent?
    ): IBinder? {
        intent ?: return null

        mClientStreamCamera?.host = intent.getStringExtra(
            EXTRA_HOST
        )?.toInetAddress()

        val logical = intent.getStringExtra(
            EXTRA_CAMERA_ID_LOGICAL
        ) ?: return null

        val physical = intent.getStringExtra(
            EXTRA_CAMERA_ID_PHYSICAL
        )


        Log.d(TAG, "startStream: $logical: $physical")

        mStreamCamera?.start(
            MSCameraModelID(
                logical,
                physical,
                false,
                MSManagerCamera(
                    baseContext
                ).getCharacteristics(
                    physical ?: logical
                )
            ),
            intent.getIntExtra(
                EXTRA_VIDEO_WIDTH,
                0
            ),
            intent.getIntExtra(
                EXTRA_VIDEO_HEIGHT,
                0
            ),
            Handler(
                mThread!!.looper
            )
        )

        mServerRestorePackets?.start()

        return null
    }
}