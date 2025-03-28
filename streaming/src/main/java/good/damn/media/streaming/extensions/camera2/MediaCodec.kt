package good.damn.media.streaming.extensions.camera2

import android.media.MediaCodec
import android.os.Build
import android.os.Handler

inline fun MediaCodec.setCallbackCompat(
    callback: MediaCodec.Callback,
    handler: Handler? = null
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setCallback(
            callback,
            handler
        )
        return
    }

    setCallback(
        callback
    )
}