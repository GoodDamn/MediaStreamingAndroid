package good.damn.editor.mediastreaming.network.server

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.camera.avc.MSCoder
import good.damn.editor.mediastreaming.camera.avc.MSDecoderAvc
import good.damn.editor.mediastreaming.extensions.short
import good.damn.editor.mediastreaming.extensions.toFraction
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFramePiece

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
    ) = mDecoder.configure(
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

    fun start() = mDecoder.start()
    fun stop() = mDecoder.stop()
    fun release() = mDecoder.release()

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        mDecoder.writeData(
            data
        )
    }

}