package good.damn.editor.mediastreaming.system.service.serv

import android.app.Service
import android.content.Intent
import good.damn.media.streaming.service.MSServiceStreamImpl

class MSServiceStream
: Service() {

    private val mImpl = MSServiceStreamImpl()

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        mImpl.startCommand(
            applicationContext
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