package good.damn.editor.mediastreaming.camera

import android.util.Log
import good.damn.editor.mediastreaming.camera.avc.MSCameraAVC
import good.damn.editor.mediastreaming.camera.avc.MSUtilsAvc
import good.damn.editor.mediastreaming.camera.avc.MSUtilsAvc.Companion.LEN_META
import good.damn.editor.mediastreaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.setIntegerOnPosition
import good.damn.editor.mediastreaming.extensions.setShortOnPosition
import good.damn.editor.mediastreaming.network.client.MSClientStreamUDPChunk
import good.damn.editor.mediastreaming.network.client.MSModelChunkUDP
import kotlinx.coroutines.CoroutineScope
import java.net.InetAddress

class MSStreamCameraInput(
    manager: MSManagerCamera,
    scope: CoroutineScope
): MSListenerOnGetFrameData {

    companion object {
        private val TAG = MSStreamCameraInput::class.simpleName
    }

    private val mStream = MSClientStreamUDPChunk(
        5556,
        scope
    )

    private val mCamera = MSCameraAVC(
        manager,
        this@MSStreamCameraInput
    )

    val isRunning: Boolean
        get() = mCamera.isRunning

    var host: InetAddress
        get() = mStream.host
        set(v) {
            mStream.host = v
        }

    private var mPacketId = 0

    fun start(
        cameraId: MSCameraModelID,
        width: Int,
        height: Int
    ) {
        mCamera.apply {
            configure(
                width,
                height,
                cameraId.characteristics
            )

            start(
                cameraId
            )
        }

        mStream.start()
    }

    fun stop() {
        mCamera.stop()
        mStream.stop()
    }

    fun release() {
        mCamera.release()
        mStream.release()
    }

    override fun onGetFrameData(
        bufferData: ByteArray,
        offset: Int,
        len: Int
    ) {
        var i = offset

        val normLen = len / 1024 * 1024
        val reminderDataSize = len - normLen

        while (i < normLen) {
            val chunk = ByteArray(1024 + LEN_META) // 1024 data + 6 meta

            Log.d(TAG, "onGetFrameData: $mPacketId=1024")

            chunk.setIntegerOnPosition(
                mPacketId,
                pos=MSUtilsAvc.OFFSET_PACKET_ID
            )

            chunk.setShortOnPosition(
                1024,
                MSUtilsAvc.OFFSET_PACKET_SIZE
            )

            for (j in 0 until 1024) {
                chunk[j+LEN_META] = bufferData[i+j]
            }

            Thread.sleep(2)
            mStream.sendToStream(
                MSModelChunkUDP(
                    chunk,
                    0,
                    chunk.size
                )
            )

            i += 1024
            mPacketId++
        }

        if (reminderDataSize > 0) {
            Log.d(TAG, "onGetFrameData: $mPacketId=$reminderDataSize")
            val chunk = ByteArray(
                reminderDataSize + LEN_META
            )

            chunk.setIntegerOnPosition(
                mPacketId,
                pos=MSUtilsAvc.OFFSET_PACKET_ID
            )

            chunk.setShortOnPosition(
                reminderDataSize,
                MSUtilsAvc.OFFSET_PACKET_SIZE
            )

            for (j in 0 until reminderDataSize) {
                chunk[j+LEN_META] = bufferData[i+j]
            }
            
            mStream.sendToStream(
                MSModelChunkUDP(
                    chunk,
                    0,
                    chunk.size
                )
            )
        }

        mPacketId++
    }
}