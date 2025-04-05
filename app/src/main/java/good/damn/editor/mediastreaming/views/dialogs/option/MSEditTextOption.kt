package good.damn.editor.mediastreaming.views.dialogs.option

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText

class MSEditTextOption(
    context: Context,
    val optionWatcher: MSOptionTextWatcher
): AppCompatEditText(
    context
) {
    init {
        addTextChangedListener(
            optionWatcher
        )
    }
}