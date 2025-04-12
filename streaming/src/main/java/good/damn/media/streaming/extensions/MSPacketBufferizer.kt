package good.damn.media.streaming.extensions

import good.damn.media.streaming.MSStreamConstantsPacket
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer

inline fun MSPacketBufferizer.writeDefault(
    data: ByteArray
) = write(
    data.integerBE(
        MSStreamConstantsPacket.OFFSET_PACKET_FRAME_ID
    ),
    data.short(
        MSStreamConstantsPacket.OFFSET_PACKET_ID
    ).toShort(),
    data.short(
        MSStreamConstantsPacket.OFFSET_PACKET_COUNT
    ).toShort(),
    data
)