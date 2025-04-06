package good.damn.media.streaming.extensions

import java.io.InputStream
import java.io.OutputStream

inline fun Int.toByteArray() = byteArrayOf(
    (this shr 24 and 0xff).toByte(),
    (this shr 16 and 0xff).toByte(),
    (this shr 8 and 0xff).toByte(),
    (this and 0xff).toByte(),
)

inline fun Int.write(
    os: OutputStream
) {
    var v = this
    for (i in 0 until 4) {
        os.write(
            v and 0xff
        )
        v = v shr 8
    }
    // 0 - 32 - 24
    // 1 - 24 - 16
    // 2 - 16 - 8
    // 3 - 8 - 0
}
