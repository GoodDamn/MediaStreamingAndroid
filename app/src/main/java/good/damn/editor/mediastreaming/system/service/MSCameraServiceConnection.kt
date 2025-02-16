package good.damn.editor.mediastreaming.system.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class MSCameraServiceConnection
: ServiceConnection {


    override fun onServiceConnected(
        name: ComponentName?,
        service: IBinder?
    ) {

    }

    override fun onServiceDisconnected(
        name: ComponentName?
    ) {

    }
}