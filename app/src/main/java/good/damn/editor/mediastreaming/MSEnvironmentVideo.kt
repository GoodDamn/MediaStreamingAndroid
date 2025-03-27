package good.damn.editor.mediastreaming

import android.content.Context
import android.media.MediaFormat
import android.util.Log
import android.util.Size
import android.view.Surface
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.avc.cache.MSListenerOnOrderPacket
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.camera2.default
import good.damn.media.streaming.network.server.udp.MSPacketMissingHandler
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrame
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress

class MSEnvironmentVideo(
    private val mServiceStreamWrapper: MSServiceStreamWrapper
): MSListenerOnOrderPacket {

    companion object {
        private const val TAG = "MSStreamEnvironmentCame"
    }

    val resolution = Size(
        1280,
        720
    )

    val isReceiving: Boolean
        get() = mServerVideo.isRunning


    val isStreamingVideo: Boolean
        get() = mServiceStreamWrapper
            .serviceConnectionStream
            .binder
            ?.isStreamingCamera ?: false

    private val mReceiverFrame = MSReceiverCameraFrame()

    private val mHandlerPacketMissing = MSPacketMissingHandler()

    private val mBufferizerRemote = MSPacketBufferizer().apply {
        onGetOrderedFrame = mReceiverFrame
        mReceiverFrame.bufferizer = this
        mHandlerPacketMissing.bufferizer = this
    }

    private val mServerVideo = MSServerUDP(
        MSStreamConstants.PORT_VIDEO,
        MSStreamCameraInput.PACKET_MAX_SIZE + MSUtilsAvc.LEN_META,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverFrame
    )


    private val mServerRestorePackets = MSServerUDP(
        MSStreamConstants.PORT_VIDEO_RESTORE,
        MSStreamCameraInput.PACKET_MAX_SIZE + MSUtilsAvc.LEN_META,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverFrame
    )

    fun startReceiving(
        surfaceOutput: Surface,
        host: InetAddress
    ) {
        mHandlerPacketMissing.host = host

        mReceiverFrame.configure(
            surfaceOutput,
            MediaFormat.createVideoFormat(
                MSCoder.TYPE_AVC,
                resolution.width,
                resolution.height
            ).apply {
                default()
                setInteger(
                    MediaFormat.KEY_ROTATION,
                    90
                )
            }
        )

        // Bufferizing
        mServerVideo.start()
        mServerRestorePackets.start()

        mBufferizerRemote.onOrderPacket = this@MSEnvironmentVideo

        CoroutineScope(
            Dispatchers.IO
        ).launch {
            while (mServerVideo.isRunning) {
                mBufferizerRemote.orderPacket()
            }

            mBufferizerRemote.clear()
        }
    }

    fun stopReceiving() {
        if (!mServerVideo.isRunning) {
            return
        }

        mReceiverFrame.stop()
        mServerVideo.stop()
        mServerRestorePackets.stop()
        mHandlerPacketMissing.isRunning = false
    }

    fun releaseReceiving() {
        mReceiverFrame.release()
        mServerVideo.release()
        mServerRestorePackets.release()

        mServerVideo.apply {
            stop()
            release()
        }
    }

    fun stopStreamingCamera() = mServiceStreamWrapper
        .serviceConnectionStream
        .binder
        ?.stopStreamingCamera()


    fun startStreamingCamera(
        idLogical: String,
        idPhysical: String?,
        host: String
    ) = mServiceStreamWrapper
        .serviceConnectionStream
        .binder
        ?.startStreamingVideo(
            idLogical,
            idPhysical,
            host,
            resolution.width,
            resolution.height
        )

    override fun onOrderPacket(
        currentFrameId: Int
    ) {
        Log.d(TAG, "onCreateView: MSListenerOnOrderPacket: $currentFrameId")
        if (currentFrameId > 15 && !mHandlerPacketMissing.isRunning) {
            // Checking
            mHandlerPacketMissing.handlingMissedPackets()
        }

        if (currentFrameId > 30 && !mReceiverFrame.isDecoding) {
            // Decoding
            mReceiverFrame.startDecoding()
            mBufferizerRemote.onOrderPacket = null;
        }
    }
}