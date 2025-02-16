package good.damn.media.streaming.extensions

fun Byte.toFraction() = (toInt() and 0xff) / 255.toFloat()