package good.damn.editor.mediastreaming.views

import android.content.Context
import android.media.MediaFormat
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.network.server.MSReceiverCameraFrame
import good.damn.media.streaming.network.server.MSServerUDP

class MSViewStreamFrame(
    context: Context,
    private val receiverFrame: MSReceiverCameraFrame,
    private val serverUdp: MSServerUDP,
    private val videoFormat: MediaFormat
): SurfaceView(
    context
), SurfaceHolder.Callback {

    companion object {
        private const val TAG = "MSViewStreamFrame"
    }

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
        Log.d(TAG, "surfaceChanged: ")

        receiverFrame.configure(
            holder.surface,
            videoFormat
        )

        receiverFrame.start()
        serverUdp.start()
    }

    override fun surfaceDestroyed(
        holder: SurfaceHolder
    ) {
        Log.d(TAG, "surfaceDestroyed: ")
    }

}