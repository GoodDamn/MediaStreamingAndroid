package good.damn.media.streaming.service.impl

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import good.damn.media.streaming.models.MSMStream
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamCameraInputConfig
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.client.MSClientUDP
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.LinkedList

class MSServiceStreamImplVideo {

    companion object {
        private const val TAG = "MSServiceStreamImplVide"
    }

    private var mStreamCamera: MSStreamCameraInput? = null
    private var mServerRestorePackets: MSServerUDP? = null
    private var mThread: HandlerThread? = null
    private var mHandler: Handler? = null
    private var mClientStreamCamera: MSClientUDP? = null

    private val mBufferizerLocal = MSPacketBufferizer()
    private val mClientDefragment = MSClientUDPDefragment(
        mBufferizerLocal
    )

    private val mSubsStreamConfig = LinkedList<
        MSStreamCameraInputConfig
    >()

    fun removeObserverStreamConfig(
        sub: MSStreamCameraInputConfig
    ) = mSubsStreamConfig.remove(sub)

    fun observeStreamConfig(
        sub: MSStreamCameraInputConfig
    ) = mSubsStreamConfig.add(sub)

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
            subscribersFrame = arrayListOf(
                mClientDefragment
            )
            subscribersConfig = mSubsStreamConfig
        }

        mServerRestorePackets = MSServerUDP(
            MSStreamConstants.PORT_VIDEO_RESTORE_REQUEST,
            64,
            CoroutineScope(
                Dispatchers.IO
            ),
            MSReceiverCameraFrameRestore().apply {
                bufferizer = mBufferizerLocal
            }
        )
    }

    fun setCanSendFrames(
        canSend: Boolean
    ) {
        mClientDefragment.client = if (
            canSend
        ) mClientStreamCamera else null
    }

    fun startStreamingCamera(
        stream: MSMStream
    ) {
        mClientDefragment.userId = stream.userId
        mClientStreamCamera?.host = stream.host.toInetAddress()
        mServerRestorePackets?.start()
        mStreamCamera?.start(
            stream.camera,
            stream.format,
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
}