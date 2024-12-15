package good.damn.editor.mediastreaming.network.server.listeners

import android.graphics.Bitmap

interface MSListenerOnReceiveFramePiece {
    suspend fun onReceiveFrame(
        frame: Bitmap,
        rotation: Int
    )
}