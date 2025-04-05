package good.damn.editor.mediastreaming.views.dialogs.option

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import good.damn.editor.mediastreaming.MSApp
import good.damn.media.streaming.MSTypeDecoderSettings

class MSDialogOptionsH264
: DialogFragment() {

    companion object {
        private const val TAG = "MSDialogOptionsH264"
    }

    var optionsTotal: ((MSTypeDecoderSettings) -> Unit)? = null

    private var mAdapter: MSAdapterOptions? = null

    override fun onCancel(dialog: DialogInterface) {
        mAdapter?.options?.apply {
            val map = HashMap<String, Int>(size)

            forEach {
                if (it.key.isNullOrBlank() || it.value.isNullOrBlank()) {
                    return@forEach
                }

                Log.d(TAG, "onCancel: ${it.key}")

                map[it.key!!] = it.value?.toInt() ?: 0
            }

            optionsTotal?.invoke(
                map
            )
        }
        super.onCancel(dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LinearLayout(
        context
    ).apply {

        val width = MSApp.width * 0.8f

        layoutParams = FrameLayout.LayoutParams(
            width.toInt(),
            -2
        )

        orientation = LinearLayout.VERTICAL

        AppCompatTextView(
            context
        ).apply {
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                MSApp.width * 0.06f
            )

            gravity = Gravity.CENTER

            text = "Codec options"

            addView(
                this,
                -1,
                -2
            )
        }


        RecyclerView(
            context
        ).let {
            it.layoutParams = LinearLayout.LayoutParams(
                -1,
                (MSApp.height * 0.3f).toInt()
            )

            it.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )

            mAdapter = MSAdapterOptions(
                width
            )

            it.adapter = mAdapter

            addView(it)
        }


    }

}