package good.damn.media.streaming.camera

import android.media.MediaFormat
import android.view.Surface
import good.damn.media.streaming.camera.avc.MSDecoderAvc
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.camera.avc.cache.MSListenerOnGetOrderedFrame
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.integer
import good.damn.media.streaming.extensions.short
import good.damn.media.streaming.extensions.writeDefault

class MSStreamSubscriberSurface
: MSStreamSubscriber,
MSListenerOnGetOrderedFrame {

    private val mDecoder = MSDecoderAvc()
    private val mBufferizer = MSPacketBufferizer().apply {
        onGetOrderedFrame = this@MSStreamSubscriberSurface
    }

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) = mDecoder.configure(
        decodeSurface,
        format
    )

    fun start() = mDecoder.start()
    fun stop() = mDecoder.stop()
    fun release() = mDecoder.release()

    override fun onGetPacket(
        data: ByteArray
    ) {
        mBufferizer.writeDefault(
            data
        )
    }

    override fun onGetOrderedFrame(
        frame: MSFrame
    ) {
        mDecoder.addOrderedFrame(
            frame
        )
    }
}