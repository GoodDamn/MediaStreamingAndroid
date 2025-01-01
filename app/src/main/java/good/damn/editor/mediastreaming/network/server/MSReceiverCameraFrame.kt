package good.damn.editor.mediastreaming.network.server

import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import good.damn.editor.mediastreaming.camera.avc.MSCoder
import good.damn.editor.mediastreaming.camera.avc.MSDecoderAvc
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData

class MSReceiverCameraFrame
: MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    private val mDecoder = MSDecoderAvc()

    fun configure(
        decodeSurface: Surface,
        width: Int,
        height: Int,
        rotation: Int
    ) {
        mDecoder.configure(
            decodeSurface,
            MediaFormat.createVideoFormat(
                MSCoder.TYPE_AVC,
                width,
                height
            ).apply {
                setInteger(
                    MediaFormat.KEY_ROTATION,
                    rotation
                )
            }
        )
    }

    fun start() = mDecoder.start()

    fun stop() {
        mDecoder.stop()
    }

    fun release() {
        mDecoder.release()
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        mDecoder.writeData(
            data
        )
    }

}