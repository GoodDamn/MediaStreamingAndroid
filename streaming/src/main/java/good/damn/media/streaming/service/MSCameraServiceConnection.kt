package good.damn.media.streaming.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo
import good.damn.media.streaming.service.impl.MSListenerOnConnectUser
import good.damn.media.streaming.service.impl.MSListenerOnSuccessHandshake

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

    fun requestConnectedUsers() = mBinder
        ?.requestConnectedUsers()

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo
    ) = mBinder?.sendHandshakeSettings(
        model
    )

    fun setCanSendFrames(
        canReceive: Boolean
    ) = mBinder?.setCanSendFrames(
        canReceive
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
        onConnectUser = null
        onSuccessHandshake = null
        mBinder = null
    }

}