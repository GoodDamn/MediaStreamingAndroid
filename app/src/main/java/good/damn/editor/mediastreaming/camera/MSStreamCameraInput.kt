package good.damn.editor.mediastreaming.camera

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnUpdateCameraFrame
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameData
import good.damn.editor.mediastreaming.extensions.setIntegerOnPosition
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.client.MSClientStreamUDP
import good.damn.media.gles.gl.textures.GLTexture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.InetAddress

class MSStreamCameraInput(
    context: Context,
    private val texture: GLTexture,
    private val sendDtMs: Long = 16L
): MSStateable, MSListenerOnGetCameraFrameData {

    companion object {
        private val TAG = MSStreamCameraInput::class.simpleName
    }

    private val mBufferPixels = IntArray(
        texture.width * texture.height
    )

    private val mClientCamera = MSClientStreamUDP(
        5556,
        CoroutineScope(
            Dispatchers.IO
        )
    )

    private val mCamera = MSCamera(
        texture.width,
        texture.height,
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

    var onUpdateCameraFrame: MSListenerOnUpdateCameraFrame? = null

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

        decodedBitmap.getPixels(
            mBufferPixels,
            0,
            decodedBitmap.width,
            0,
            0,
            decodedBitmap.width,
            decodedBitmap.height
        ) // ARGB

        val colorBuf = ByteArray(4)

        var arrIndex = 0
        var bufIndex = 0

        while (arrIndex < mBufferPixels.size) {
            colorBuf.setIntegerOnPosition(
                mBufferPixels[arrIndex++],
                pos = 0
            )

            colorBuf.forEach {
                texture.buffer.put(
                    bufIndex++,
                    it
                )
            }
        }

        onUpdateCameraFrame?.apply {
            MSApp.ui {
                onUpdateFrame()
            }
        }

        mCurrentTimeMs = System.currentTimeMillis()
        if (sendDtMs > mCurrentTimeMs - mPrevTimeMs) {
            return
        }

        Log.d(TAG, "onGetFrame: ${data.size}")

        
        
        /*mClientCamera.sendToStream(
            data
        )*/

        mPrevTimeMs = mCurrentTimeMs
    }

}