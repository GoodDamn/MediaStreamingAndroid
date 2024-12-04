package good.damn.editor.mediastreaming.camera

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameBitmap
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameData
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.client.MSClientStreamUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.InetAddress

class MSStreamCameraInput(
    width: Int,
    height: Int,
    context: Context,
    private val sendDtMs: Long = 16L
): MSStateable, MSListenerOnGetCameraFrameData {

    companion object {
        private val TAG = MSStreamCameraInput::class.simpleName
    }

    private val mClientCamera = MSClientStreamUDP(
        5556,
        CoroutineScope(
            Dispatchers.IO
        )
    )

    private val mCamera = MSCamera(
        width,
        height,
        context
    ).apply {
        onGetCameraFrame = this@MSStreamCameraInput
    }

    private var mCurrentTimeMs = System.currentTimeMillis()
    private var mPrevTimeMs = mCurrentTimeMs

    val rotation: Int
        get() = mCamera.rotation

    var host: InetAddress
        get() = mClientCamera.host
        set(v) {
            mClientCamera.host = v
        }

    var onGetCameraFrameBitmap: MSListenerOnGetCameraFrameBitmap? = null

    override fun start(): Job {
        mCamera.openCameraStream()
        return mClientCamera.start()
    }

    override fun stop() {
        mCamera.stop()
        mClientCamera.stop()
    }

    override fun release() {
        mCamera.release()
        mClientCamera.release()
    }

    override fun onGetFrame(
        data: ByteArray
    ) {
        val decodedBitmap = BitmapFactory.decodeByteArray(
            data,
            0,
            data.size
        )

        onGetCameraFrameBitmap?.onGetFrameBitmap(
            decodedBitmap
        )

        mCurrentTimeMs = System.currentTimeMillis()
        if (sendDtMs > mCurrentTimeMs - mPrevTimeMs) {
            return
        }


        Log.d(TAG, "onGetFrame: ${data.size}")

        mClientCamera.sendToStream(
            data
        )

        mPrevTimeMs = mCurrentTimeMs
    }

}