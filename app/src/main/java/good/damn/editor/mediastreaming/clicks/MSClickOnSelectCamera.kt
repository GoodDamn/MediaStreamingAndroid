package good.damn.editor.mediastreaming.clicks

import android.view.View
import good.damn.media.streaming.camera.models.MSMCameraId

class MSClickOnSelectCamera(
    val cameraId: MSMCameraId
): View.OnClickListener {

    var onSelectCamera: MSListenerOnSelectCamera? = null

    override fun onClick(
        v: View?
    ) {
        onSelectCamera?.onSelectCamera(
            v,
            cameraId
        )
    }

}