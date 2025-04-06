package good.damn.editor.mediastreaming

import android.content.Context
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.camera.avc.MSDecoderAvc
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.camera2.default
import good.damn.media.streaming.network.client.tcp.MSNetworkDecoderSettings
import good.damn.media.streaming.network.server.tcp.MSServerTCP
import good.damn.media.streaming.network.server.udp.MSPacketMissingHandler
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrame
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress

class MSEnvironmentVideo(
    private val mServiceWrapper: MSServiceStreamWrapper
): Runnable {

    companion object {
        const val TIMEOUT_DEFAULT_PACKET_MS = 33;
        const val INTERVAL_MISS_PACKET = TIMEOUT_DEFAULT_PACKET_MS / 3
        private const val TAG = "MSStreamEnvironmentCame"
    }


    val isReceiving: Boolean
        get() = mServerVideo.isRunning

    val isStreamingVideo: Boolean
        get() = mServiceWrapper.isStreamingVideo

    var hostTo: InetAddress?
        get() = mHandlerPacketMissing.host
        private set(v) {
            mHandlerPacketMissing.host = v
        }

    private val mReceiverFrame = MSReceiverCameraFrame()

    private val mDecoderVideo = MSDecoderAvc()

    private val mHandlerPacketMissing = MSPacketMissingHandler()

    private val mBufferizerRemote = MSPacketBufferizer().apply {
        mReceiverFrame.bufferizer = this
    }

    private val mServerVideo = MSServerUDP(
        MSStreamConstants.PORT_MEDIA,
        MSStreamConstants.PACKET_MAX_SIZE,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverFrame
    )

    private var mThreadDecoding: HandlerThread? = null
    private var mHandlerDecoding: Handler? = null

    fun startReceiving(
        surfaceOutput: Surface,
        format: MediaFormat,
        host: InetAddress?
    ) {
        hostTo = host
        mHandlerDecoding?.apply {
            removeCallbacks(
                this@MSEnvironmentVideo
            )
            post {
                startDecoder(
                    surfaceOutput,
                    format
                )
            }
            return
        }

        HandlerThread(
            "decodingEnvironment"
        ).apply {
            start()
            mThreadDecoding = this

            mHandlerDecoding = Handler(
                looper
            )

            startDecoder(
                surfaceOutput,
                format
            )
        }
    }

    fun stopReceiving() {
        if (!mServerVideo.isRunning) {
            return
        }
        mDecoderVideo.stop()
        mServerVideo.stop()
    }

    fun releaseReceiving() {
        mDecoderVideo.release()
        mServerVideo.release()

        mThreadDecoding?.quit()
        mThreadDecoding = null
        mHandlerDecoding = null

        mServerVideo.apply {
            stop()
            release()
        }
    }

    fun stopStreamingCamera() = mServiceWrapper
        .stopStreamingVideo()

    fun startStreamingCamera(
        cameraId: MSCameraModelID,
        host: String,
        mediaFormat: MediaFormat,
    ) = mServiceWrapper.startStreamingVideo(
        cameraId,
        mediaFormat,
        host
    )

    private fun startDecoder(
        surfaceOutput: Surface,
        format: MediaFormat
    ) {
        mDecoderVideo.configure(
            surfaceOutput,
            format
        )

        // Bufferizing
        mServerVideo.start()

        mHandlerDecoding?.post(
            this@MSEnvironmentVideo
        )

        mDecoderVideo.start()
    }

    override fun run() {
        if (!mServerVideo.isRunning) {
            mBufferizerRemote.clear()
            return
        }

        val queue = mBufferizerRemote.orderedQueue
        val frame = if (
            queue.isEmpty()
        ) null else queue.removeFirst()

        if (frame == null) {
            mHandlerDecoding?.post(
                this
            )
            return
        }

        var capturedTime: Long
        var currentTime: Long

        capturedTime = System.currentTimeMillis()

        var currentPacketSize = frame.packetsAdded.toInt()
        var delta: Long
        var nextPartMissed = 0L

        val timeout = if (
            currentPacketSize >= 8
        ) TIMEOUT_DEFAULT_PACKET_MS * 10 else TIMEOUT_DEFAULT_PACKET_MS

        do {
            currentTime = System.currentTimeMillis()
            delta = currentTime - capturedTime

            if (frame.packetsAdded > currentPacketSize) {
                currentPacketSize = frame.packetsAdded.toInt()
                capturedTime = currentTime
            }

            // Waiting when frame will be combined
            // if it's not, drop it because of timeout
            if (currentPacketSize >= frame.packets.size) {
                mDecoderVideo.addFrame(
                    frame
                )
                break
            }

            if (delta > nextPartMissed) {
                nextPartMissed += INTERVAL_MISS_PACKET
                    .toLong()
                mHandlerPacketMissing.handlingMissedPackets(
                    mBufferizerRemote
                )
            }
        } while (delta < timeout)

        if (!mServerVideo.isRunning) {
            mBufferizerRemote.clear()
            return
        }

        mHandlerDecoding?.post(
            this
        )
    }

}