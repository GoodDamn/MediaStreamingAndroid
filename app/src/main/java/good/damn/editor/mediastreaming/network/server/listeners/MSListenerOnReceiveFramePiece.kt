package good.damn.editor.mediastreaming.network.server.listeners

import android.graphics.Bitmap

interface MSListenerOnReceiveFramePiece {
    suspend fun onReceiveFrame(
        userId: Byte,
        roomId: Byte,
        frame: Bitmap,
        rotation: Int
    )
}