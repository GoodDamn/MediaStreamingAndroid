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
    scope: CoroutineScope,
    scaledDownWidth: Int,
    scaledDownHeight: Int,
    private val texture: GLTexture
): MSStateable,
MSListenerOnGetCameraFrameData {

    companion object {
        private val TAG = MSStreamCameraInput::class.simpleName
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

    private val mIntervalPixel =
        texture.width * texture.height /
        (scaledDownWidth * scaledDownHeight)

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
            mClientCamera,
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
    }

}