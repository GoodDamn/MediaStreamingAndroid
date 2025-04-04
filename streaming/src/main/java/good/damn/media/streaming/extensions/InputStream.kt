package good.damn.media.streaming.extensions

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

inline fun InputStream.readU() = read() and 0xff