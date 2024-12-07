package good.damn.editor.mediastreaming.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.core.graphics.get
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnUpdateCameraFrame
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameData
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.setIntegerOnPosition
import good.damn.editor.mediastreaming.extensions.setShortOnPosition
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.client.MSClientStreamUDP
import good.damn.editor.mediastreaming.network.client.MSClientStreamUDPChunk
import good.damn.editor.mediastreaming.network.client.MSModelChunkUDP
import good.damn.editor.mediastreaming.out.stream.MSOutputStreamBuffer
import good.damn.editor.mediastreaming.utils.MSUtilsImage
import good.damn.media.gles.gl.textures.GLTexture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.nio.Buffer
import java.nio.ByteBuffer

class MSStreamCameraInput(
    manager: MSManagerCamera,
    scope: CoroutineScope,
    private val texture: GLTexture
): MSStateable,
MSListenerOnGetCameraFrameData {

    companion object {
        private val TAG = MSStreamCameraInput::class.simpleName
    }

    private val mClientCamera = MSClientStreamUDPChunk(
        5556,
        scope
    )

    private val mCamera = MSCamera(
        manager
    ).apply {
        onGetCameraFrame = this@MSStreamCameraInput
    }

    private val mBuffer = ByteArray(
        60000
    )

    private var mScaleBuffer = ByteArray(0)
    private var mScaleBufferStream = MSOutputStreamBuffer()

    val rotation: Int
        get() = mCamera.rotation

    var cameraId: MSCameraModelID?
        get() = mCamera.cameraId
        set(v) {
            mCamera.cameraId = v
        }

    var host: InetAddress
        get() = mClientCamera.host
        set(v) {
            mClientCamera.host = v
        }

    var onUpdateCameraFrame: MSListenerOnUpdateCameraFrame? = null

    override fun start() {
        if (mCamera.openCameraStream()) {
            mClientCamera.start()
        }
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
        jpegPlane: Image.Plane,
    ) {
        val buffer = jpegPlane.buffer
        val bufSize = buffer.capacity()

        if (bufSize >= mBuffer.size-2) {
            if (bufSize >= mScaleBuffer.size) {
                mScaleBuffer = ByteArray(
                    bufSize
                )
                mScaleBufferStream.buffer = mScaleBuffer
            }

            buffer.get(
                mScaleBuffer,
                0,
                bufSize
            )

            var scaleBufferSize = mScaleBuffer.size
            while (scaleBufferSize > mBuffer.size-2) {
                val bb = BitmapFactory.decodeByteArray(
                    mScaleBuffer,
                    0,
                    scaleBufferSize
                ) ?: return

                val resultBitmap = Bitmap.createScaledBitmap(
                    bb,
                    360,
                    240,
                    false
                )

                mScaleBufferStream.position = 0
                mScaleBufferStream.offset = 2

                resultBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    mScaleBufferStream
                )

                scaleBufferSize = mScaleBufferStream.position

                resultBitmap.recycle()
            }

            Log.d(TAG, "onGetFrame: $scaleBufferSize ${mScaleBuffer.size} ${mBuffer.size}")

            mScaleBuffer.setShortOnPosition(
                scaleBufferSize,
                pos = 0
            )

            mClientCamera.sendToStream(
                MSModelChunkUDP(
                    mScaleBuffer,
                    scaleBufferSize + 2
                )
            )

            return
        }

        mBuffer.setShortOnPosition(
            bufSize,
            pos = 0
        )

        buffer.get(
            mBuffer,
            2,
            bufSize
        )

        Log.d(TAG, "onGetFrame: STATIC: $bufSize")

        mClientCamera.sendToStream(
            MSModelChunkUDP(
                mBuffer,
                bufSize + 2
            )
        )
    }

}