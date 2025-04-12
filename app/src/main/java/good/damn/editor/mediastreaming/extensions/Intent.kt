package good.damn.editor.mediastreaming.extensions

import android.content.Intent

inline fun Intent.removeString(
    name: String
) = getStringExtra(name).run {
    if (this == null) {
        return@run null
    }
    removeExtra(name)
    return@run this
}

inline fun Intent.removeInt(
    name: String
) = getIntExtra(name, 0).run {
    removeExtra(name)
    return@run this
}