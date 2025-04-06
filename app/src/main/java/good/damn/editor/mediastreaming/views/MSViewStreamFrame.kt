package good.damn.editor.mediastreaming.views

import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class MSViewStreamFrame(
    context: Context?
): SurfaceView(
    context
), SurfaceHolder.Callback {

    companion object {
        private const val TAG = "MSViewStreamFrame"
    }

    var onChangeSurface: MSListenerOnChangeSurface? = null

    init {
        holder.addCallback(
            this
        )
    }

    override fun surfaceCreated(
        holder: SurfaceHolder
    ) {
        Log.d(TAG, "surfaceCreated: ")
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        Log.d(TAG, "surfaceChanged: ${holder.surface}")
        onChangeSurface?.onChangeSurface(
            holder.surface
        )
    }

    override fun surfaceDestroyed(
        holder: SurfaceHolder
    ) {
        Log.d(TAG, "surfaceDestroyed: ")
    }

}