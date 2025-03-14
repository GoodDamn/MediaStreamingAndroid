package good.damn.media.streaming.network.server

import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore.Audio.Media
import android.view.Surface
import good.damn.media.streaming.camera.avc.MSDecoderAvc
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveData
import java.nio.ByteBuffer

class MSReceiverCameraFrame
: MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    private val mDecoder = MSDecoderAvc()

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) {
        mDecoder.configure(
            decodeSurface,
            format
        )
    }

    fun start() {
        mDecoder.start()
    }

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