package good.damn.editor.mediastreaming.extensions

fun Byte.toFraction() = (toInt() and 0xff) / 255.toFloat()