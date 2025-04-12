package good.damn.media.streaming

class MSStreamConstantsPacket {
    companion object {
        const val LEN_META = 14

        const val OFFSET_PACKET_FRAME_ID = 0
        const val OFFSET_PACKET_SIZE = 4
        const val OFFSET_PACKET_ID = 6
        const val OFFSET_PACKET_COUNT = 8
        const val OFFSET_PACKET_SRC_ID = 10
    }
}