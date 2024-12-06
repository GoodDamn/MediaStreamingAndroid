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
import good.damn.editor.mediastreaming.extensions.setIntegerOnPosition
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
    context: Context,
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
        context
    ).apply {
        onGetCameraFrame = this@MSStreamCameraInput
    }

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

    private val mBuffer = ByteArray(
        60000
    )

    private var mScaleBuffer = ByteArray(0)
    private var mScaleBufferStream = MSOutputStreamBuffer()

    override fun onGetFrame(
        jpegPlane: Image.Plane,
    ) {
        val buffer = jpegPlane.buffer
        val bufSize = buffer.capacity()

        if (bufSize >= mBuffer.size-4) {
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
            while (scaleBufferSize > mBuffer.size-4) {
                val bb = BitmapFactory.decodeByteArray(
                    mScaleBuffer,
                    0,
                    scaleBufferSize
                )

                val resultBitmap = Bitmap.createScaledBitmap(
                    bb,
                    320,
                    240,
                    false
                )

                mScaleBufferStream.position = 0
                mScaleBufferStream.offset = 4

                resultBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    mScaleBufferStream
                )

                scaleBufferSize = mScaleBufferStream.position

                resultBitmap.recycle()
            }

            Log.d(TAG, "onGetFrame: $scaleBufferSize ${mScaleBuffer.size} ${mBuffer.size}")

            mScaleBuffer.setIntegerOnPosition(
                scaleBufferSize,
                pos = 0
            )

            mClientCamera.sendToStream(
                MSModelChunkUDP(
                    mScaleBuffer,
                    scaleBufferSize + 4
                )
            )

            return
        }

        Log.d(TAG, "onGetFrame: STATIC: $bufSize")
        mBuffer.setIntegerOnPosition(
            bufSize,
            pos = 0
        )

        buffer.get(
            mBuffer,
            4,
            bufSize
        )

        mClientCamera.sendToStream(
            MSModelChunkUDP(
                mBuffer,
                bufSize + 4
            )
        )
    }

}