package good.damn.media.streaming.camera

import android.media.MediaFormat
import android.view.Surface
import good.damn.media.streaming.camera.avc.MSDecoderAvc

class MSStreamSubscriberSurface
: MSStreamSubscriber {

    private val mDecoder = MSDecoderAvc()

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
        mDecoder.writeData(
            data
        )
    }
}