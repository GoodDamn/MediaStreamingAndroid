package good.damn.editor.mediastreaming.camera.listeners

import android.graphics.Bitmap

interface MSListenerOnGetCameraFrameBitmap {
    fun onGetFrameBitmap(
        bitmap: Bitmap
    )
}