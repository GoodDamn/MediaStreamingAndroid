package good.damn.media.streaming.camera

import android.media.MediaFormat
import android.os.Handler
import good.damn.media.streaming.camera.avc.MSCameraAVC
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.media.streaming.camera.models.MSMCameraId
import java.nio.ByteBuffer

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

    var subscribersFrame: List<MSStreamCameraInputFrame>? = null
    var subscribersConfig: List<MSStreamCameraInputConfig>? = null

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
        mFrameId = 0
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
            subscribersConfig?.forEach {
                it.onGetCameraConfigStream(
                    bufferData,
                    offset,
                    len
                )
            }
            mFrameId++
            return
        }

        subscribersFrame?.forEach {
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