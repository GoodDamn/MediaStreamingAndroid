package good.damn.editor.mediastreaming.system.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class MSCameraServiceConnection
: ServiceConnection {

    companion object {
        private const val TAG = "MSCameraServiceConnecti"
    }
    
    var binder: MSServiceStreamBinder? = null
        private set

    override fun onServiceConnected(
        name: ComponentName?,
        service: IBinder?
    ) {
        binder = service as? MSServiceStreamBinder
        Log.d(TAG, "onServiceConnected: ")
    }

    override fun onServiceDisconnected(
        name: ComponentName?
    ) {
        binder = null
    }
}