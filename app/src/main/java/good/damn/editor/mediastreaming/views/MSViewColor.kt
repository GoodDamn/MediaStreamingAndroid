package good.damn.editor.mediastreaming.views

import android.content.Context
import android.graphics.Canvas
import android.view.View
import androidx.annotation.ColorInt

class MSViewColor(
    context: Context
): View(
    context
) {

    @ColorInt
    var color: Int = 0xff000000.toInt()
        private set

    override fun setBackgroundColor(
        color: Int
    ) {
        this.color = color
        invalidate()
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        canvas.drawColor(
            color
        )
    }

}