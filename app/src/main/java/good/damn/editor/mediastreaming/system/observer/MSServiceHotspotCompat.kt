package good.damn.editor.mediastreaming.system.observer

import android.content.Context
import android.os.Build
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost

class MSServiceHotspotCompat(
    context: Context
) {

    var delegate: MSListenerOnGetHotspotHost? = null
        set(value) {
            mService.delegate = value
            field = value
        }

    private val mService = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    ) MSObserverHotspotApi30(context)
    else MSObserverHotspot(context)

    fun start() {
        mService.start()
    }
}