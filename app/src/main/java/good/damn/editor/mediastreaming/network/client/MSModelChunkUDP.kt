package good.damn.editor.mediastreaming.network.client

data class MSModelChunkUDP(
    val data: ByteArray,
    val len: Int
) {
    override fun toString() = "DATA_SIZE: ${data.size} LEN: $len"
}