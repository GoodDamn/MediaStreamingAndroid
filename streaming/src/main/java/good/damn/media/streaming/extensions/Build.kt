package good.damn.media.streaming.extensions

import android.os.Build

inline fun hasUpOsVersion(
    vers: Int
) = Build.VERSION.SDK_INT >= vers