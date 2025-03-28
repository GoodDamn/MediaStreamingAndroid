package good.damn.media.streaming.extensions.camera2

import android.media.MediaCodecInfo
import android.media.MediaFormat

inline fun MediaFormat.default() {
    setInteger(
        MediaFormat.KEY_COLOR_FORMAT,
        MediaCodecInfo.CodecCapabilities
            .COLOR_FormatSurface
    )

    setInteger(
        MediaFormat.KEY_BIT_RATE,
        4000 * 8
    )

    setInteger(
        MediaFormat.KEY_FRAME_RATE,
        1
    )

    setInteger(
        MediaFormat.KEY_I_FRAME_INTERVAL,
        1
    )
}