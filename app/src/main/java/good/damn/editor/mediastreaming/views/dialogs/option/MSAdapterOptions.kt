package good.damn.editor.mediastreaming.views.dialogs.option

import android.media.MediaFormat
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import good.damn.media.streaming.MSTypeDecoderSettings

class MSAdapterOptions(
    private val widthItem: Float,
    src: MSTypeDecoderSettings
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_OPTION = 0
        private const val TYPE_ADD = 1
    }

    val options: List<MSMOption>
        get() = mOptions

    private val mOptions = ArrayList<MSMOption>(
        src.size
    ).apply {
        src.forEach {
            add(
                MSMOption(
                    it.key,
                    it.value.toString()
                )
            )
        }
    }

    override fun getItemViewType(
        position: Int
    ) = when (position) {
        in 0 until mOptions.size -> TYPE_OPTION
        else -> TYPE_ADD
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        else -> MSViewHolderOption.create(
            parent.context,
            widthItem
        )
    }

    override fun getItemCount() = mOptions.size

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) = when (holder) {
        is MSViewHolderOption -> {
            if (position < mOptions.size) {
                holder.model = mOptions[position]
            }
            Unit
        }

        else -> Unit
    }

}