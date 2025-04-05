package good.damn.media.streaming.extensions

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

inline fun InputStream.waitNonBlock(): Boolean {
    val time = System.currentTimeMillis()

    while (available() == 0) {
        if (System.currentTimeMillis() - time > 3000L) {
            return false
        }
    }

    return true
}

inline fun InputStream.readU() = read() and 0xff

inline fun InputStream.readUSafely() = readSafely() and 0xff

inline fun InputStream.readSafely(
    arr: ByteArray,
    offset: Int = 0,
    len: Int = arr.size
) = if (waitNonBlock()) read(
    arr,
    offset,
    len
) else -1

inline fun InputStream.readSafely() = if (
    waitNonBlock()
) read() else -1
