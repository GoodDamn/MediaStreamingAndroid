package good.damn.media.streaming.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.media.MediaFormat
import android.os.IBinder
import android.util.Log
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.models.MSCameraModelID

class MSCameraServiceConnection
: ServiceConnection {

    companion object {
        private const val TAG = "MSCameraServiceConnecti"
    }
    
    private var mBinder: MSServiceStreamBinder? = null

    var onSuccessHandshake: MSListenerOnSuccessHandshake?
        get() = mBinder?.onSuccessHandshake
        set(v) {
            mBinder?.onSuccessHandshake = v
        }

    fun sendHandshakeSettings(
        host: String,
        settings: MSTypeDecoderSettings
    ) = mBinder?.sendHandshakeSettings(
        host,
        settings
    )

    fun startStreamingVideo(
        modelID: MSCameraModelID,
        mediaFormat: MediaFormat,
        host: String
    ) = mBinder?.startStreamingCamera(
        modelID,
        mediaFormat,
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