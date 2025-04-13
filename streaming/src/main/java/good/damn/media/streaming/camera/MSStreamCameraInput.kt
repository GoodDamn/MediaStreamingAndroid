package good.damn.media.streaming.camera

import android.media.MediaFormat
import android.os.Handler
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.camera.avc.MSCameraAVC
import good.damn.media.streaming.MSStreamConstantsPacket
import good.damn.media.streaming.MSStreamConstantsPacket.Companion.LEN_META
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.media.streaming.camera.models.MSMCameraId
import good.damn.media.streaming.extensions.setIntegerOnPosition
import good.damn.media.streaming.extensions.setShortOnPosition
import java.nio.ByteBuffer
import kotlin.random.Random

class MSStreamCameraInput(
    manager: MSManagerCamera
): MSListenerOnGetFrameData {

    companion object {
        private val TAG = MSStreamCameraInput::class.simpleName
    }

    private val mCamera = MSCameraAVC(
        manager,
        this@MSStreamCameraInput
    )

    var subscribers: List<MSStreamCameraInputSubscriber>? = null

    val isRunning: Boolean
        get() = mCamera.isRunning

    private var mFrameId = 0

    fun start(
        cameraId: MSMCameraId,
        mediaFormat: MediaFormat,
        handler: Handler
    ) = mCamera.run {
        configure(
            mediaFormat,
            handler
        )

        start(
            cameraId,
            handler
        )
    }

    fun stop() {
        mCamera.stop()
    }

    fun release() {
        mCamera.release()
    }

    final override fun onGetFrameData(
        bufferData: ByteBuffer,
        offset: Int,
        len: Int
    ) {
        if (mFrameId == 0) {
            subscribers?.forEach {
                it.onGetCameraConfigStream(
                    bufferData,
                    offset,
                    len
                )
            }
            //mFrameId++
            //return
        }

        subscribers?.forEach {
            it.onGetCameraFrame(
                mFrameId,
                bufferData,
                offset,
                len
            )
        }

        mFrameId++
    }

}