package good.damn.editor.mediastreaming.system.service.serv

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import good.damn.editor.mediastreaming.system.service.MSServiceStreamBinder
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriber
import good.damn.media.streaming.network.client.MSClientUDP
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSServiceStream
: Service() {

    private val mImpl = MSServiceStreamImpl()

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        mImpl.startCommand(
            baseContext
        )
        return START_NOT_STICKY
    }

    override fun onBind(
        intent: Intent?
    ) = mImpl.getBinder()

    override fun onDestroy() {
        mImpl.destroy()
    }
}