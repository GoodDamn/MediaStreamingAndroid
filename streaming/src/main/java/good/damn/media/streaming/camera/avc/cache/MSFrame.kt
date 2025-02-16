package good.damn.media.streaming.camera.avc.cache

data class MSFrame(
    val id: Int,
    val packets: Array<MSPacket?>,
    var packetsAdded: Short
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MSFrame

        if (id != other.id) return false
        if (!packets.contentEquals(other.packets)) return false
        if (packetsAdded != other.packetsAdded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + packets.contentHashCode()
        result = 31 * result + packetsAdded
        return result
    }

    override fun toString() = "ID: $id; PACKETS_SIZE: ${packetsAdded}=${packets.size}"
}