package good.damn.editor.mediastreaming.camera

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.core.graphics.get
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnUpdateCameraFrame
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameData
import good.damn.editor.mediastreaming.extensions.setIntegerOnPosition
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.client.MSClientStreamUDP
import good.damn.editor.mediastreaming.utils.MSUtilsImage
import good.damn.media.gles.gl.textures.GLTexture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.InetAddress
import java.nio.Buffer
import java.nio.ByteBuffer

class MSStreamCameraInput(
    context: Context,
    private val texture: GLTexture,
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
        yPlane: Image.Plane,
        uPlane: Image.Plane,
        vPlane: Image.Plane
    ) {
        MSUtilsImage.fromYUVtoARGB(
            yPlane.buffer,
            uPlane.buffer,
            vPlane.buffer,
            texture.buffer,
            yPlane.rowStride,
            yPlane.pixelStride,
            uPlane.rowStride,
            uPlane.pixelStride,
            texture.width,
            texture.height
        )

        onUpdateCameraFrame?.apply {
            MSApp.ui {
                onUpdateFrame()
            }
        }

        mCurrentTimeMs = System.currentTimeMillis()
        if (sendDtMs > mCurrentTimeMs - mPrevTimeMs) {
            return
        }

        /*mClientCamera.sendToStream(
            data
        )*/

        mPrevTimeMs = mCurrentTimeMs
    }

    /*override fun onGetFrame(
        yBuffer: ByteBuffer,
        uBuffer: ByteBuffer,
        vBuffer: ByteBuffer
    ) {
        Log.d(TAG, "onGetFrame: ${yBuffer.capacity()} ${uBuffer.capacity()} ${vBuffer.capacity()} ${texture.buffer.capacity()}")

        MSUtilsImage.fromYUVtoARGB(
            yBuffer,
            uBuffer,
            vBuffer,
            texture.buffer,

        )

        var arrIndex = 0
        var yIndex = 0
        val dt = 1.0 / texture.buffer.capacity()
        var factor = 0.0

        var v: Byte
        var u: Byte
        var bufferPtr: Int

        while (arrIndex < texture.buffer.capacity()) {
            bufferPtr = (factor * yBuffer.capacity()).toInt()
            v = (vBuffer[bufferPtr] - 128).toByte()
            u = (uBuffer[bufferPtr] - 128).toByte()

            factor += dt

            // This interesting shit needs NDK with C lang
            texture.buffer.put(
                arrIndex,
                255.toByte()
            ) // A

            texture.buffer.put(
                arrIndex + 1,
                (yBuffer[yIndex] + 1.3707f * v)
                    .toInt().toByte()
            ) // R

            texture.buffer.put(
                arrIndex + 2,
                (yBuffer[yIndex] - 0.698f * v - 0.58f * u)
                    .toInt().toByte()
            ) // G

            texture.buffer.put(
                arrIndex + 3,
                (yBuffer[yIndex] + 1.732f * u)
                    .toInt().toByte()
            ) // B


            arrIndex += 4
            yIndex++
        }

        var arrIndex = 0
        var bufIndex = 0

        while (arrIndex < mBufferPixels.size) {
            texture.buffer.put(
                bufIndex++,
                buffer
            )
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

        /*mClientCamera.sendToStream(
            data
        )*/

        mPrevTimeMs = mCurrentTimeMs
    }*/

}