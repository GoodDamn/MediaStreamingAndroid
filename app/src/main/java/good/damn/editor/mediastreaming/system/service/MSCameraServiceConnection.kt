package good.damn.editor.mediastreaming.system.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost
import good.damn.media.streaming.camera.models.MSCameraModelID

class MSCameraServiceConnection
: ServiceConnection {

    companion object {
        private const val TAG = "MSCameraServiceConnecti"
    }
    
    private var mBinder: MSServiceStreamBinder? = null

    fun startStreamingVideo(
        modelID: MSCameraModelID,
        width: Int,
        height: Int,
        host: String
    ) = mBinder?.startStreamingCamera(
        modelID,
        width,
        height,
        host
    )

    fun stopStreamingVideo() = mBinder
        ?.stopStreamingCamera()

    override fun onServiceConnected(
        name: ComponentName?,
        service: IBinder?
    ) {
        mBinder = service as? MSServiceStreamBinder
        Log.d(TAG, "onServiceConnected: ")
    }

    override fun onServiceDisconnected(
        name: ComponentName?
    ) {
        Log.d(TAG, "onServiceDisconnected: ")
        mBinder = null
    }

}