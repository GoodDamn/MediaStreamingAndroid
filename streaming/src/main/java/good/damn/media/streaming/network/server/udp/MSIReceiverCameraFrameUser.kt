package good.damn.media.streaming.network.server.udp

import android.media.MediaFormat
import android.os.Handler
import android.view.Surface
import android.view.SurfaceView
import java.net.InetAddress

interface MSIReceiverCameraFrameUser {

    val surfaceView: SurfaceView

    fun setConfigFrame(
        data: ByteArray
    )

    fun receiveUserFrame(
        data: ByteArray
    )

    fun startReceive(
        userId: Int,
        surfaceOutput: Surface,
        format: MediaFormat,
        host: InetAddress?,
        handler: Handler
    )
    fun release()
    fun stop()
}