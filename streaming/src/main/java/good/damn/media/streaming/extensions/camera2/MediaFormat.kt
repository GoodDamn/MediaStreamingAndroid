package good.damn.media.streaming.extensions.camera2

import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecProfileLevel
import android.media.MediaFormat
import android.os.Build
import android.provider.MediaStore.Audio.Media

inline fun MediaFormat.default() {
    setInteger(
        MediaFormat.KEY_COLOR_FORMAT,
        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
    )

    setInteger(
        MediaFormat.KEY_BIT_RATE,
        512 * 8
    )

    setInteger(
        MediaFormat.KEY_FRAME_RATE,
        1
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        setInteger(
            MediaFormat.KEY_COLOR_STANDARD,
            MediaFormat.COLOR_STANDARD_BT2020
        )

        setInteger(
            MediaFormat.KEY_COLOR_TRANSFER,
            MediaFormat.COLOR_TRANSFER_LINEAR
        )

        setInteger(
            MediaFormat.KEY_COLOR_RANGE,
            MediaFormat.COLOR_RANGE_LIMITED
        )
    }

    setInteger(
        MediaFormat.KEY_I_FRAME_INTERVAL,
        -1
    )
}