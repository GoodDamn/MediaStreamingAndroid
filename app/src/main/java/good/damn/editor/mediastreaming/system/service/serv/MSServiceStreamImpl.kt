package good.damn.editor.mediastreaming.system.service.serv

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import good.damn.editor.mediastreaming.system.service.MSServiceStreamBinder
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriber
import good.damn.media.streaming.network.client.MSClientUDP
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSServiceStreamImpl
: MSStreamSubscriber {

    companion object {
        private const val TAG = "MSServiceStreamImpl"
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

    fun startCommand(
        context: Context
    ) {
        Log.d(TAG, "startCommand: ")
        mThread = HandlerThread(
            "communicationThread"
        ).apply {
            start()
        }

        mClientStreamCamera = MSClientUDP(
            MSStreamConstants.PORT_MEDIA
        )

        mStreamCamera = MSStreamCameraInput(
            MSManagerCamera(
                context
            )
        ).apply {
            subscribers = arrayListOf(
                this@MSServiceStreamImpl
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
    }

    fun getBinder() = mBinder
    
    fun destroy() {
        Log.d(TAG, "destroy: ")
        mStreamCamera?.stop()
        mServerRestorePackets?.stop()

        mThread?.quitSafely()
        mThread = null

        mStreamCamera?.release()
        mClientStreamCamera?.release()
        mServerRestorePackets?.release()
    }

    override fun onGetPacket(
        data: ByteArray
    ) {
        mClientStreamCamera?.sendToStream(
            data
        )
    }
}