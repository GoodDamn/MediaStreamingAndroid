package good.damn.media.streaming.extensions

import android.os.Build

inline fun hasOsVersion(
    vers: Int
) = Build.VERSION.SDK_INT >= vers