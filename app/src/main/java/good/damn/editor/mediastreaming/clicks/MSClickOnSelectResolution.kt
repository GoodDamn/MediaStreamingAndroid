package good.damn.editor.mediastreaming.clicks

import android.util.Size
import android.view.View
import good.damn.media.streaming.camera.models.MSCameraModelID

class MSClickOnSelectResolution(
    private val resolution: Size,
    private val cameraId: MSCameraModelID
): View.OnClickListener {

    var onSelectResolution: MSListenerOnSelectResolution? = null

    override fun onClick(
        v: View?
    ) = onSelectResolution?.onSelectResolution(
        resolution,
        cameraId
    ) ?: Unit

}