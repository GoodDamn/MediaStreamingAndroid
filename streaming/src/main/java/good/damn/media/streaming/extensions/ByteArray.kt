package good.damn.media.streaming.extensions

inline fun ByteArray.setShortOnPosition(
    i: Int,
    pos: Int = 0
) {
    set(pos, ((i shr 8) and 0xff).toByte())
    set(pos+1, (i and 0xff).toByte())
}

inline fun ByteArray.short(
    offset: Int = 0
) = (get(offset).toInt() and 0xff shl 8) or
    (get(offset+1).toInt() and 0xff)

inline fun ByteArray.integerLE(
    offset: Int
) = (get(offset+3).toInt() and 0xff shl 24) or
    (get(offset+2).toInt() and 0xff shl 16) or
    (get(offset+1).toInt() and 0xff shl 8) or
    (get(offset).toInt() and 0xff)

inline fun ByteArray.integerBE(
    offset: Int
) = (get(offset).toInt() and 0xff shl 24) or
    (get(offset+1).toInt() and 0xff shl 16) or
    (get(offset+2).toInt() and 0xff shl 8) or
    (get(offset+3).toInt() and 0xff)

inline fun ByteArray.setIntegerOnPosition(
    i: Int,
    pos: Int
) {
    set(pos, ((i shr 24) and 0xff).toByte())
    set(pos+1, ((i shr 16) and 0xff).toByte())
    set(pos+2, ((i shr 8) and 0xff).toByte())
    set(pos+3, (i and 0xff).toByte())
}