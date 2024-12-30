package good.damn.editor.mediastreaming.camera

import good.damn.editor.mediastreaming.camera.avc.MSCameraAVC
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

        var chunkCount = len / 1024
        val normLen = chunkCount * 1024

        val reminderDataSize = len - normLen

        if (reminderDataSize > 0) {
            chunkCount++
        }

        var chunkId = 0
        while (i < normLen) {
            val chunk = ByteArray(1034) // 1024 data + 10 meta

            chunk.setIntegerOnPosition(
                mPacketId,
                pos=0
            )

            chunk.setShortOnPosition(
                chunkId,
                4
            )

            chunk.setShortOnPosition(
                chunkCount,
                6
            )

            chunk.setShortOnPosition(
                1024,
                8
            )

            for (j in 10 until 1034) {
                chunk[j] = bufferData[i+j]
            }

            mStream.sendToStream(
                MSModelChunkUDP(
                    chunk,
                    0,
                    chunk.size
                )
            )

            i += 1024
            chunkId++
        }

        if (reminderDataSize > 0) {
            val chunk = ByteArray(
                reminderDataSize + 10
            )

            chunk.setIntegerOnPosition(
                mPacketId,
                pos=0
            )

            chunk.setShortOnPosition(
                chunkId,
                4
            )

            chunk.setShortOnPosition(
                chunkCount,
                6
            )

            chunk.setShortOnPosition(
                reminderDataSize,
                8
            )

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