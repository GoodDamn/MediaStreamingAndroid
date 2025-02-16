package good.damn.media.streaming.network.server.listeners

import android.graphics.Bitmap

interface MSListenerOnReceiveFramePiece {
    suspend fun onReceiveFrame(
        data: ByteArray
    )
}