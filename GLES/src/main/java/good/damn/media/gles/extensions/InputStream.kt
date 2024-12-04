package good.damn.media.gles.extensions

import java.io.ByteArrayOutputStream
import java.io.InputStream

fun InputStream.readAllBytesCompat(): ByteArray {
    val baos = ByteArrayOutputStream()

    val buffer = ByteArray(
        8192
    )
    var n: Int

    while (true) {
        n = read(buffer)
        if (n == -1) {
            break
        }

        baos.write(
            buffer,
            0,
            n
        )
    }

    close()
    val bytes = baos.toByteArray()
    baos.close()

    return bytes
}