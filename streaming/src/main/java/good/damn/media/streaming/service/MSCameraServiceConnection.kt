package good.damn.media.streaming.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.media.MediaFormat
import android.os.IBinder
import android.util.Log
import good.damn.media.streaming.MSMStream
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.models.MSMCameraId

class MSCameraServiceConnection
: ServiceConnection {

    companion object {
        private const val TAG = "MSCameraServiceConnecti"
    }
    
    private var mBinder: MSServiceStreamBinder? = null

    var onConnectUser: MSListenerOnConnectUser? = null
        set(v) {
            field = v
            mBinder?.onConnectUser = v
        }

    var onSuccessHandshake: MSListenerOnSuccessHandshake?
        get() = mBinder?.onSuccessHandshake
        set(v) {
            mBinder?.onSuccessHandshake = v
        }

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo
    ) = mBinder?.sendHandshakeSettings(
        model
    )

    fun startStreamingVideo(
        stream: MSMStream
    ) = mBinder?.startStreamingCamera(
        stream
    )

    fun stopStreamingVideo() = mBinder
        ?.stopStreamingCamera()

    override fun onServiceConnected(
        name: ComponentName?,
        service: IBinder?
    ) {
        mBinder = service as? MSServiceStreamBinder
        mBinder?.onConnectUser = onConnectUser
        Log.d(TAG, "onServiceConnected: ")
    }

    override fun onServiceDisconnected(
        name: ComponentName?
    ) {
        Log.d(TAG, "onServiceDisconnected: ")
        mBinder?.onConnectUser = null
        mBinder?.onSuccessHandshake = null
        mBinder = null
    }

}