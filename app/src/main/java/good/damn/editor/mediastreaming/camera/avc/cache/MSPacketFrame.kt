package good.damn.editor.mediastreaming.camera.avc.cache

import java.util.LinkedList

data class MSPacketFrame(
    val chunks: Array<MSPacket?>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MSPacketFrame

        return chunks.contentEquals(other.chunks)
    }

    override fun hashCode(): Int {
        return chunks.contentHashCode()
    }
}