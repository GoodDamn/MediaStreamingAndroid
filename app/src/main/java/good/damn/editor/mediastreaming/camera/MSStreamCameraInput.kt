package good.damn.editor.mediastreaming.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.Log
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnUpdateCameraFrame
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnGetCameraFrameData
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.setShortOnPosition
import good.damn.editor.mediastreaming.network.MSStateable
import good.damn.editor.mediastreaming.network.client.MSClientStreamUDPChunk
import good.damn.editor.mediastreaming.network.client.MSModelChunkUDP
import good.damn.editor.mediastreaming.out.stream.MSOutputStreamBuffer
import kotlinx.coroutines.CoroutineScope
import java.net.InetAddress

class MSStreamCameraInput(
    manager: MSManagerCamera,
    private val mRoomId: Byte,
    scope: CoroutineScope,
    private val mUserId: Byte
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

    private var mScaleBuffer = ByteArray(1024 * 1024)
    private var mScaleBufferStream = MSOutputStreamBuffer().apply {
        buffer = mScaleBuffer
    }
    private val mBitmapOffset = 5

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

        if (bufSize >= mBuffer.size-mBitmapOffset) {
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
            var scaleBufferOffset = 0

            while (scaleBufferSize > mBuffer.size-mBitmapOffset) {
                val bb = BitmapFactory.decodeByteArray(
                    mScaleBuffer,
                    scaleBufferOffset,
                    scaleBufferSize
                )

                val resultBitmap = Bitmap.createScaledBitmap(
                    bb,
                    360,
                    240,
                    true
                )

                scaleBufferOffset = mBitmapOffset
                mScaleBufferStream.position = 0
                mScaleBufferStream.offset = mBitmapOffset

                resultBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    75,
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

            setMeta(mScaleBuffer)

            mClientCamera.sendToStream(
                MSModelChunkUDP(
                    mScaleBuffer,
                    scaleBufferSize + mBitmapOffset
                )
            )

            return
        }

        mBuffer.setShortOnPosition(
            bufSize,
            pos = 0
        )

        setMeta(
            mBuffer
        )

        buffer.get(
            mBuffer,
            mBitmapOffset,
            bufSize
        )

        Log.d(TAG, "onGetFrame: STATIC: $bufSize")

        mClientCamera.sendToStream(
            MSModelChunkUDP(
                mBuffer,
                bufSize + mBitmapOffset
            )
        )
    }

    private inline fun setMeta(
        buffer: ByteArray
    ) {
        buffer[2] = (
            rotation.toFloat() / 360f * 255
        ).toInt().toByte()

        buffer[3] = mUserId;
        buffer[4] = mRoomId;
    }

}