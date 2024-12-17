package good.damn.editor.mediastreaming.network.server

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import good.damn.editor.mediastreaming.extensions.short
import good.damn.editor.mediastreaming.extensions.toFraction
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFramePiece

class MSReceiverCameraFrame
: MSListenerOnReceiveData {

    var onReceiveFramePiece: MSListenerOnReceiveFramePiece? = null

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val bitmapSize = data.short(
            offset = 0
        )

        val rotation = (
            data[2].toFraction() * 360
        ).toInt()

        Log.d(TAG, "onReceiveData: $bitmapSize ${data.size} $rotation")

        onReceiveFramePiece?.onReceiveFrame(
            data[3],
            data[4],
            BitmapFactory.decodeByteArray(
                data,
                5,
                bitmapSize
            ),
            rotation
        )
    }

}