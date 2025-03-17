package good.damn.media.streaming.extensions

import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer

inline fun MSPacketBufferizer.writeDefault(
    data: ByteArray
) = write(
    data.integer(
        MSUtilsAvc.OFFSET_PACKET_FRAME_ID
    ),
    data.short(
        MSUtilsAvc.OFFSET_PACKET_ID
    ).toShort(),
    data.short(
        MSUtilsAvc.OFFSET_PACKET_COUNT
    ).toShort(),
    data
)