package good.damn.editor.mediastreaming.network.server.listeners

import android.graphics.Bitmap

interface MSListenerOnReceiveFramePiece {
    suspend fun onReceiveFramePiece(
        from: Int,
        to: Int,
        offsetPixels: Int,
        pixels: ByteArray
    )
}