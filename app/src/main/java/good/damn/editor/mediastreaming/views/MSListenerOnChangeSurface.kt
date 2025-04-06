package good.damn.editor.mediastreaming.views

import android.view.Surface

fun interface MSListenerOnChangeSurface {
    fun onChangeSurface(
        surface: Surface
    )
}