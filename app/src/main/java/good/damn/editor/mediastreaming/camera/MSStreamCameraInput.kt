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
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.nio.Buffer
import java.nio.ByteBuffer

class MSStreamCameraInput(
    context: Context,
    private val scope: CoroutineScope,
    private val texture: GLTexture,
    private val sendDtMs: Long = 16L
): MSStateable, MSListenerOnGetCameraFrameData {

    companion object {
        private val TAG = MSStreamCameraInput::class.simpleName
        const val PIXEL_COUNT_SEND = 12000
        const val PIXEL_COLORS_SEND = PIXEL_COUNT_SEND * 4
    }

    private val mClientCamera = MSClientStreamUDP(
        5556,
        scope
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

        scope.launch {
            texture.buffer.apply {
                val capacity = capacity()
                var bufIndex = 0
                var fromIndex = 0
                var toIndex = PIXEL_COLORS_SEND
                var buffer = ByteArray(
                    8 + PIXEL_COLORS_SEND
                )
                var limit = PIXEL_COLORS_SEND

                var i = 0
                while (i < capacity) {
                    if (bufIndex >= limit) {
                        buffer.setIntegerOnPosition(
                            fromIndex,
                            pos = 0
                        )

                        buffer.setIntegerOnPosition(
                            toIndex,
                            pos = 4
                        )
                        mClientCamera.sendToStream(
                            buffer
                        )
                        buffer = ByteArray(
                            8 + PIXEL_COLORS_SEND
                        )
                        fromIndex += bufIndex
                        bufIndex = 0
                        limit = if (capacity - i < PIXEL_COLORS_SEND)
                            capacity - i
                        else PIXEL_COLORS_SEND
                        toIndex = i + limit
                        continue
                    }

                    buffer[8 + bufIndex++] = get(i)
                    i++
                }
            }
        }

        onUpdateCameraFrame?.apply {
            MSApp.ui {
                onUpdateFrame()
            }
        }

        mCurrentTimeMs = System.currentTimeMillis()
//        if (sendDtMs > mCurrentTimeMs - mPrevTimeMs) {
//            return
//        }
//
//        mClientCamera.sendToStream(
//            data
//        )

        mPrevTimeMs = mCurrentTimeMs
    }

}