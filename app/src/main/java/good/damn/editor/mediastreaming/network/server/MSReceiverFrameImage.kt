package good.damn.editor.mediastreaming.network.server

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MSReceiverFrameImage
: MSListenerOnReceiveData {

    var onReceiveFrame: MSListenerOnReceiveFrame? = null

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val result = BitmapFactory.decodeByteArray(
            data,
            0,
            data.size
        )

        withContext(
            Dispatchers.Main
        ) {
            onReceiveFrame?.onReceiveFrame(
                result
            )
        }
    }

}