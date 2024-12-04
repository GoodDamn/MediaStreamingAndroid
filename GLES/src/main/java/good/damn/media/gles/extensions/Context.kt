package good.damn.media.gles.extensions

import android.content.Context
import androidx.annotation.RawRes
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

fun Context.rawText(
    @RawRes id: Int
) = String(
    resources.openRawResource(
        id
    ).readAllBytesCompat(),
    Charset.forName(
        "UTF-8"
    )
)