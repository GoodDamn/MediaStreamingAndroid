package good.damn.media.streaming.extensions

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

inline fun InputStream.readU() = read() and 0xff

fun InputStream.readString(
    len: Int
): String {
    val arr = ByteArray(len)
    var offset = 0
    var n: Int
    while (true) {
        n = read(arr, offset, len - offset)
        if (n <= 0) {
            break
        }
        offset += n
    }

    return String(
        arr,
        0,
        len,
        Charset.forName(
            "UTF-8"
        )
    )
}