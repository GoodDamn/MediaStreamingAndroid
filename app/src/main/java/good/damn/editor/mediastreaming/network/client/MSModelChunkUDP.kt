package good.damn.editor.mediastreaming.network.client

data class MSModelChunkUDP(
    val data: ByteArray,
    val offset: Int,
    val len: Int
) {
    override fun toString() = "DATA_SIZE: ${data.size} OFFSET: $offset LEN: $len"
}