package good.damn.editor.mediastreaming

import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.camera.avc.MSDecoderAvc
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.avc.cache.MSFrame
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
): Runnable {

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

    private val mDecoderVideo = MSDecoderAvc()

    private val mHandlerPacketMissing = MSPacketMissingHandler()

    private val mBufferizerRemote = MSPacketBufferizer().apply {
        mReceiverFrame.bufferizer = this
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

    private var mThreadDecoding: HandlerThread? = null
    private var mHandlerDecoding: Handler? = null

    fun startReceiving(
        surfaceOutput: Surface,
        host: InetAddress
    ) {
        mHandlerPacketMissing.host = host

        mThreadDecoding = HandlerThread(
            "decodingEnvironment"
        ).apply {
            start()

            mHandlerDecoding = Handler(
                looper
            )

            mHandlerDecoding?.post {
                mDecoderVideo.configure(
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

                mHandlerDecoding?.post(
                    this@MSEnvironmentVideo
                )

                mDecoderVideo.start()
            }
        }
    }

    fun stopReceiving() {
        if (!mServerVideo.isRunning) {
            return
        }

        mDecoderVideo.stop()
        mServerVideo.stop()
        mServerRestorePackets.stop()
    }

    fun releaseReceiving() {
        mDecoderVideo.release()
        mServerVideo.release()
        mServerRestorePackets.release()

        mHandlerDecoding?.removeCallbacks(
            this
        )

        mThreadDecoding?.interrupt()

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

    override fun run() {
        Log.d(TAG, "run: ")
        if (!mServerVideo.isRunning) {
            mBufferizerRemote.clear()
            return
        }

        val frame = mBufferizerRemote.orderedFrame
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
                mBufferizerRemote.removeFirstFrameFromQueue(
                    frame
                )
                break
            }

            if (delta > nextPartMissed) {
                nextPartMissed += MSPacketBufferizer
                    .INTERVAL_MISS_PACKET
                    .toLong()
                mHandlerPacketMissing.handlingMissedPackets(
                    mBufferizerRemote
                )
            }
        } while (delta < MSPacketBufferizer.TIMEOUT_DEFAULT_PACKET_MS)

        mHandlerDecoding?.post(
            this
        )
    }

}