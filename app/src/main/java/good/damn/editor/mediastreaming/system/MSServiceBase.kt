package good.damn.editor.mediastreaming.system

import android.content.Context

abstract class MSServiceBase<DELEGATE>(
    val context: Context
) {
    var delegate: DELEGATE? = null
    abstract fun start()
}