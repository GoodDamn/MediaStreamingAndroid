package good.damn.media.streaming.service.impl

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import good.damn.media.streaming.models.MSMStream
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamCameraInputSubscriber
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.client.MSClientUDP
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.nio.ByteBuffer
import kotlin.random.Random

class MSServiceStreamImplVideo
: MSStreamCameraInputSubscriber,
MSListenerOnEachDefragmentedPacket {

    companion object {
        private const val TAG = "MSServiceStreamImplVide"
    }
    
    private var mStreamCamera: MSStreamCameraInput? = null
    private var mClientStreamCamera: MSClientUDP? = null
    private var mServerRestorePackets: MSServerUDP? = null

    private var mThread: HandlerThread? = null
    private var mHandler: Handler? = null

    private var mUserId = 0
    private val mBufferizerLocal = MSPacketBufferizer()

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
                bufferizer = mBufferizerLocal
            }
        )
    }

    fun startStreamingCamera(
        stream: MSMStream
    ) {
        mUserId = stream.userId
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

    override fun onGetCameraConfigStream(
        data: ByteBuffer,
        offset: Int,
        len: Int
    ) {

    }

    override fun onGetCameraFrame(
        frameId: Int,
        data: ByteBuffer,
        offset: Int,
        len: Int
    ) {
        MSStreamPacketFragmentizer.defragmentByteArray(
            mUserId,
            frameId,
            data,
            offset,
            len,
            this
        )

        if (frameId >= MSPacketBufferizer.CACHE_PACKET_SIZE) {
            mBufferizerLocal.removeFirstFrameQueueByFrameId(
                frameId
            )
        }
    }

    override fun onEachDefragmentedPacket(
        frameId: Int,
        packetId: Short,
        packetCount: Short,
        data: ByteArray
    ) {
        mBufferizerLocal.write(
            frameId,
            packetId,
            packetCount,
            data
        )

        mClientStreamCamera?.sendToStream(
            data
        )
    }

}