package good.damn.media.streaming.env

import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import good.damn.media.streaming.camera.MSCameraCodecBuffers
import good.damn.media.streaming.camera.avc.MSDecoderAvc
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.camera.avc.cache.MSPacket
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.writeDefault
import good.damn.media.streaming.network.server.udp.MSPacketMissingHandler
import java.net.InetAddress

class MSEnvironmentVideoDecodeStream
: Runnable {

    companion object {
        const val TIMEOUT_DEFAULT_PACKET_MS = 33
        const val INTERVAL_MISS_PACKET = TIMEOUT_DEFAULT_PACKET_MS / 3
        private const val TAG = "MSStreamEnvironmentCame"
    }

    private val mCodecBuffers = MSCameraCodecBuffers()
    private val mDecoderVideo = MSDecoderAvc(mCodecBuffers)
    private val mHandlerPacketMissing = MSPacketMissingHandler()
    private val mBufferizerRemote = MSPacketBufferizer()

    private var mHandlerDecoding: Handler? = null

    var isRunning = false
        private set

    fun writeToBuffer(
        data: ByteArray
    ) = mBufferizerRemote.writeDefault(
        data
    )

    fun start(
        surfaceOutput: Surface,
        format: MediaFormat,
        host: InetAddress?,
        handler: Handler
    ) {
        mHandlerPacketMissing.host = host
        mBufferizerRemote.unlock()
        handler.apply {
            mHandlerDecoding = this
            removeCallbacks(
                this@MSEnvironmentVideoDecodeStream
            )
            post {
                startDecoder(
                    surfaceOutput,
                    format
                )
            }
        }
    }

    fun setConfigFrame(
        data: ByteArray
    ) {
        mCodecBuffers.addFrame(
            MSFrame(
                0,
                arrayOf(
                    MSPacket(
                        0,
                        data
                    )
                ),
                1
            )
        )
    }

    fun stop() {
        if (!isRunning) {
            return
        }

        mBufferizerRemote.lock()
        mBufferizerRemote.clear()
        mDecoderVideo.stop()
        mCodecBuffers.clearQueue()

        isRunning = false
    }

    fun release() {
        mDecoderVideo.release()

        mHandlerPacketMissing.release()

        mBufferizerRemote.lock()
        mBufferizerRemote.clear()

        mCodecBuffers.clearQueue()
        mHandlerDecoding = null
        isRunning = false
    }

    private fun startDecoder(
        surfaceOutput: Surface,
        format: MediaFormat
    ) {
        mDecoderVideo.configure(
            surfaceOutput,
            format
        )

        isRunning = true
        mHandlerDecoding?.post(
            this@MSEnvironmentVideoDecodeStream
        )

        mDecoderVideo.start()
    }

    override fun run() {
        if (!isRunning) {
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

        mCodecBuffers.showNextFrame(
            mDecoderVideo.codec
        )

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
                mCodecBuffers.addFrame(frame)
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

        if (!isRunning) {
            mBufferizerRemote.clear()
            return
        }

        mHandlerDecoding?.post(
            this
        )
    }

}