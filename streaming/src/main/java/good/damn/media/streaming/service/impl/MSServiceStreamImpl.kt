package good.damn.media.streaming.service.impl

import android.content.Context
import android.util.Log
import good.damn.media.streaming.service.MSServiceStreamBinder

class MSServiceStreamImpl {

    companion object {
        private const val TAG = "MSServiceStreamImpl"
        const val EXTRA_CAMERA_ID_LOGICAL = "l"
        const val EXTRA_CAMERA_ID_PHYSICAL = "P"
        const val EXTRA_VIDEO_WIDTH = "W"
        const val EXTRA_VIDEO_HEIGHT = "H"
        const val EXTRA_HOST = "h"
    }


    private val mImplHandshake = MSServiceStreamImplHandshake()

    private val mAccepterStreamConfig = MSAccepterStreamConfigHandshake(
        mImplHandshake
    )

    private val mImplVideo = MSServiceStreamImplVideo().apply {
        observeStreamConfig(
            mAccepterStreamConfig
        )

        mImplHandshake.onSuccessHandshake = this
    }

    private val mBinder = MSServiceStreamBinder(
        mImplVideo,
        mImplHandshake,
        mAccepterStreamConfig
    )

    fun startCommand(
        context: Context
    ) {
        Log.d(TAG, "startCommand: ")

        mImplVideo.startCommand(
            context
        )

        mImplHandshake.startCommand()
        mImplHandshake.startListeningSettings()
    }

    fun getBinder() = mBinder
    
    fun destroy() {
        Log.d(TAG, "destroy: ")
        mImplHandshake.destroy()
        mImplVideo.destroy()
    }
}