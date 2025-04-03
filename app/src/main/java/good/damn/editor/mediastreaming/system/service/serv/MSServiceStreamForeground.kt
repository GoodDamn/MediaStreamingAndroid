package good.damn.editor.mediastreaming.system.service.serv

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import good.damn.editor.mediastreaming.extensions.supportsForegroundService
import good.damn.editor.mediastreaming.notification.MSNotifications

class MSServiceStreamForeground
: Service() {

    companion object {
        private const val TAG = "MSServiceStreamForegrou"
    }
    
    private val mImpl = MSServiceStreamImpl()
    
    override fun onCreate() {
        super.onCreate()
        
        if (!applicationContext.supportsForegroundService()) {
            return
        }

        val notification = MSNotifications.foregroundStream(
            "Stream is running",
            "Camera is streaming",
            applicationContext
        ) ?: return

        Log.d(TAG, "onCreate: ")

        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            else 0
        )
    }

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