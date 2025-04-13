package good.damn.media.streaming.service.impl

import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.MSStreamConstantsPacket
import good.damn.media.streaming.MSStreamConstantsPacket.Companion.LEN_META
import good.damn.media.streaming.extensions.setIntegerOnPosition
import good.damn.media.streaming.extensions.setShortOnPosition
import java.nio.ByteBuffer

class MSStreamPacketFragmentizer {

    companion object {
        private const val PACKET_MAX_SIZE =
            MSStreamConstants.PACKET_MAX_SIZE - LEN_META

        fun defragmentByteArray(
            userId: Int,
            frameId: Int,
            bufferData: ByteBuffer,
            offset: Int,
            len: Int,
            onEachPacket: MSListenerOnEachDefragmentedPacket
        ) {
            var i = offset

            var packetCount = len / PACKET_MAX_SIZE
            val normLen = packetCount * PACKET_MAX_SIZE
            val reminderDataSize = len - normLen

            var packetId = 0

            if (reminderDataSize > 0) {
                packetCount++
            }

            while (i < normLen) {
                fillSendChunk(
                    userId,
                    frameId,
                    PACKET_MAX_SIZE,
                    packetId,
                    i,
                    bufferData,
                    packetCount,
                    onEachPacket
                )
                packetId++
                i += PACKET_MAX_SIZE
            }

            if (reminderDataSize > 0) {
                fillSendChunk(
                    userId,
                    frameId,
                    reminderDataSize,
                    packetId,
                    i,
                    bufferData,
                    packetCount,
                    onEachPacket
                )
            }
        }

        private inline fun fillSendChunk(
            userId: Int,
            frameId: Int,
            dataLen: Int,
            packetId: Int,
            i: Int,
            bufferData: ByteBuffer,
            packetCount: Int,
            onEachPacket: MSListenerOnEachDefragmentedPacket
        ) {
            val chunk = ByteArray(
                dataLen + LEN_META
            )

            chunk.setIntegerOnPosition(
                frameId,
                MSStreamConstantsPacket.OFFSET_PACKET_FRAME_ID
            )

            chunk.setShortOnPosition(
                dataLen,
                MSStreamConstantsPacket.OFFSET_PACKET_SIZE
            )

            chunk.setShortOnPosition(
                packetId,
                MSStreamConstantsPacket.OFFSET_PACKET_ID
            )

            chunk.setShortOnPosition(
                packetCount,
                MSStreamConstantsPacket.OFFSET_PACKET_COUNT
            )

            chunk.setIntegerOnPosition(
                userId,
                MSStreamConstantsPacket.OFFSET_PACKET_SRC_ID
            )

            for (j in 0 until dataLen) {
                chunk[j+ LEN_META] = bufferData[i+j]
            }

            onEachPacket.onEachDefragmentedPacket(
                frameId,
                packetId.toShort(),
                packetCount.toShort(),
                chunk
            )
        }

    }
}