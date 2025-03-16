package good.damn.media.streaming.camera

import android.util.Log
import good.damn.media.streaming.camera.avc.MSCameraAVC
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.avc.MSUtilsAvc.Companion.LEN_META
import good.damn.media.streaming.camera.avc.cache.MSPacket
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.camera.avc.listeners.MSListenerOnGetFrameData
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.setIntegerOnPosition
import good.damn.media.streaming.extensions.setShortOnPosition
import java.nio.ByteBuffer

class MSStreamCameraInput(
    manager: MSManagerCamera
): MSListenerOnGetFrameData {

    companion object {
        private val TAG = MSStreamCameraInput::class.simpleName
        const val PACKET_MAX_SIZE = 1024 - LEN_META
    }

    private val mCamera = MSCameraAVC(
        manager,
        this@MSStreamCameraInput
    )

    val bufferizer = MSPacketBufferizer()

    var subscribers: List<MSStreamSubscriber>? = null

    val isRunning: Boolean
        get() = mCamera.isRunning

    private var mFrameId = 0

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
        var i = offset

        var packetCount = len / PACKET_MAX_SIZE
        val normLen = packetCount * PACKET_MAX_SIZE
        val reminderDataSize = len - normLen

        var packetId = 0

        if (reminderDataSize > 0) {
            packetCount++
        }

        if (mFrameId >= MSPacketBufferizer.CACHE_PACKET_SIZE) {
            bufferizer.removeFirstFrameByIndex(
                mFrameId - MSPacketBufferizer.CACHE_PACKET_SIZE
            )
        }

        while (i < normLen) {
            fillSendChunk(
                PACKET_MAX_SIZE,
                packetId,
                i,
                bufferData,
                packetCount
            )
            packetId++
            i += PACKET_MAX_SIZE
        }

        if (reminderDataSize > 0) {
            fillSendChunk(
                reminderDataSize,
                packetId,
                i,
                bufferData,
                packetCount
            )
        }

        mFrameId++
    }

    private inline fun fillSendChunk(
        dataLen: Int,
        packetId: Int,
        i: Int,
        bufferData: ByteBuffer,
        packetCount: Int
    ) {
        val chunk = ByteArray(
            dataLen + LEN_META
        )

        Log.d(TAG, "onGetFrameData: FRAME: $mFrameId:$packetId=$dataLen")

        chunk.setIntegerOnPosition(
            mFrameId,
            pos= MSUtilsAvc.OFFSET_PACKET_FRAME_ID
        )

        chunk.setShortOnPosition(
            dataLen,
            MSUtilsAvc.OFFSET_PACKET_SIZE
        )

        chunk.setShortOnPosition(
            packetId,
            MSUtilsAvc.OFFSET_PACKET_ID
        )

        chunk.setShortOnPosition(
            packetCount,
            MSUtilsAvc.OFFSET_PACKET_COUNT
        )

        for (j in 0 until dataLen) {
            chunk[j+LEN_META] = bufferData[i+j]
        }

        bufferizer.write(
            mFrameId,
            packetId.toShort(),
            packetCount.toShort(),
            chunk
        )

        Thread.sleep(16)
        subscribers?.forEach {
            it.onGetPacket(chunk)
        }
    }
}