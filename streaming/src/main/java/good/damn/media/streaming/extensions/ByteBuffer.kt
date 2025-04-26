package good.damn.media.streaming.extensions

import java.nio.ByteBuffer

inline fun ByteBuffer?.contentToString(): String? {
    this ?: return null
    val b = StringBuilder()
    b.append("${remaining()}: ")
    for (i in 0 until remaining()) {
        b.append(get(i))
        b.append(", ")
    }

    return b.toString()
}