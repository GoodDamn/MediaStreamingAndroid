package good.damn.editor.mediastreaming.out.stream

import java.io.OutputStream

class MSOutputStreamBuffer
: OutputStream() {

    var position = 0
    var offset = 0

    var buffer: ByteArray? = null
        set(v) {
            field = v
        }

    override fun write(
        b: Int
    ) {
        buffer?.apply {
            set(offset + position, b.toByte())
            position++
        }
    }


}