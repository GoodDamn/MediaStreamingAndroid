package good.damn.editor.mediastreaming.network.server

import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore.Audio.Media
import android.view.Surface
import good.damn.editor.mediastreaming.camera.avc.MSCoder
import good.damn.editor.mediastreaming.camera.avc.MSDecoderAvc
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import java.nio.ByteBuffer

class MSReceiverCameraFrame
: MSListenerOnReceiveData {

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    private val mDecoder = MSDecoderAvc()

    private var mDecodeSurface: Surface? = null

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) {
        mDecodeSurface = decodeSurface
        mDecoder.configure(
            decodeSurface,
            format
        )
    }

    fun start() = mDecoder.start()

    fun stop() {
        mDecoder.stop()
    }

    fun release() {
        mDecodeSurface?.release()
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