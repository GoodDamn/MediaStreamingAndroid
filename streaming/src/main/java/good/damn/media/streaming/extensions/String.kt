package good.damn.media.streaming.extensions

import java.net.InetAddress

inline fun String.toInetAddress() = InetAddress.getByName(
    this
)