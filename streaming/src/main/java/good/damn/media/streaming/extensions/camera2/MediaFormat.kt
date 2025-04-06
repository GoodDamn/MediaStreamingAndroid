package good.damn.media.streaming.extensions.camera2

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaCodecInfo.CodecProfileLevel
import android.media.MediaFormat
import android.os.Build
import android.provider.MediaStore.Audio.Media
import androidx.core.location.LocationRequestCompat.Quality

inline fun MediaFormat.default() {

    setInteger(
        MediaFormat.KEY_BITRATE_MODE,
        MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR
    )

    setInteger(
        MediaFormat.KEY_COLOR_FORMAT,
        CodecCapabilities.COLOR_FormatSurface
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
}