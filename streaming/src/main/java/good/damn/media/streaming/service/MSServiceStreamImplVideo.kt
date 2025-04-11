package good.damn.media.streaming.service

import android.content.Context
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriber
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.client.MSClientUDP
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSServiceStreamImplVideo
: MSStreamSubscriber {

    private var mStreamCamera: MSStreamCameraInput? = null
    private var mClientStreamCamera: MSClientUDP? = null
    private var mServerRestorePackets: MSServerUDP? = null

    private var mThread: HandlerThread? = null
    private var mHandler: Handler? = null

    fun startCommand(
        context: Context
    ) {
        mThread = HandlerThread(
            "communicationThread"
        ).apply {
            start()

            mHandler = Handler(
                looper
            )
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
                this@MSServiceStreamImplVideo
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
    }

    fun startStreamingCamera(
        userId: Int,
        modelID: MSCameraModelID,
        mediaFormat: MediaFormat,
        host: String
    ) {
        mClientStreamCamera?.host = host.toInetAddress()
        mServerRestorePackets?.start()
        mStreamCamera?.start(
            userId,
            modelID,
            mediaFormat,
            mHandler!!
        )
    }

    fun stopStreamingCamera() {
        mServerRestorePackets?.stop()
        mStreamCamera?.stop()
    }

    fun destroy() {
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