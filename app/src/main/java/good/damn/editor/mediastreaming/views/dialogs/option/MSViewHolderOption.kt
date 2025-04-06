package good.damn.editor.mediastreaming.views.dialogs.option

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView

class MSViewHolderOption(
    private val editTextKey: MSEditTextOption,
    private val editTextValue: MSEditTextOption,
    view: View
): RecyclerView.ViewHolder(
    view
) {

    var model: MSMOption?
        get() = editTextKey.optionWatcher.model
        set(v) {

            editTextKey.setText(
                v?.key
            )

            editTextValue.setText(
                v?.value
            )

            editTextKey.optionWatcher.model = v
            editTextValue.optionWatcher.model = v
        }

    companion object {
        inline fun create(
            context: Context,
            width: Float
        ) = LinearLayout(
            context
        ).run {
            orientation = LinearLayout.HORIZONTAL

            layoutParams = LayoutParams(
                width.toInt(),
                -2
            )

            val etWidth = width * 0.4f

            return@run MSViewHolderOption(
                MSEditTextOption(
                    context,
                    MSTextWatcherOptionKey()
                ).apply {

                    hint = "Key"

                    layoutParams = LinearLayout.LayoutParams(
                        etWidth.toInt(),
                        -1
                    )

                    addView(this)
                },

                MSEditTextOption(
                    context,
                    MSTextWatcherOptionValue()
                ).apply {
                    hint = "Value"

                    layoutParams = LinearLayout.LayoutParams(
                        etWidth.toInt(),
                        -1
                    ).apply {
                        leftMargin = (width * 0.1f).toInt()
                    }

                    addView(this)
                },
                this
            )
        }
    }

}