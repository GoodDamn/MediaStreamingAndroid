package good.damn.editor.mediastreaming.system.observer

import android.content.Context

abstract class MSObserverBase<DELEGATE>(
    val context: Context
) {
    var delegate: DELEGATE? = null
    abstract fun start()
}