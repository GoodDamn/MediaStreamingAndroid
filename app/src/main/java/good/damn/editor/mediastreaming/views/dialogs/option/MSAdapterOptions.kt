package good.damn.editor.mediastreaming.views.dialogs.option

import android.media.MediaFormat
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MSAdapterOptions(
    private val widthItem: Float
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_OPTION = 0
        private const val TYPE_ADD = 1
    }

    val options: List<MSMOption>
        get() = mOptions

    private val mOptions = ArrayList<MSMOption>(
        10
    ).apply {
        add(
            MSMOption(
                MediaFormat.KEY_BIT_RATE,
                "8192"
            )
        )

        add(
            MSMOption(
                "width",
                "640"
            )
        )

        add(
            MSMOption(
                "height",
                "480"
            )
        )

        add(
            MSMOption(
                MediaFormat.KEY_ROTATION,
                "90"
            )
        )

        add(
            MSMOption(
                MediaFormat.KEY_I_FRAME_INTERVAL,
                "1"
            )
        )

        add(
            MSMOption(
                MediaFormat.KEY_FRAME_RATE,
                "1"
            )
        )

        add(
            MSMOption(
                MediaFormat.KEY_CAPTURE_RATE,
                "1"
            )
        )

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

    override fun getItemCount() = mOptions.size + 1

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