package good.damn.editor.mediastreaming.network.server.listeners

import android.graphics.Bitmap

interface MSListenerOnReceiveFrame {
    fun onReceiveFrame(
        frame: Bitmap
    )
}