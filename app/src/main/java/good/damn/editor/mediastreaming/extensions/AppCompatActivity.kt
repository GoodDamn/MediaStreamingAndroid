package good.damn.editor.mediastreaming.extensions

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

inline val AppCompatActivity.context: Context
    get() = this