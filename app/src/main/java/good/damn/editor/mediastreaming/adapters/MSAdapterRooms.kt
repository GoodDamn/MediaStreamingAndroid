package good.damn.editor.mediastreaming.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import good.damn.editor.mediastreaming.holders.MSHolderRoom
import good.damn.editor.mediastreaming.network.server.room.MSRoom
import good.damn.editor.mediastreaming.network.server.room.MSRooms

class MSAdapterRooms(
    private val rooms: List<MSRoom>
): RecyclerView.Adapter<
    MSHolderRoom
>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = MSHolderRoom.create(
        parent.context
    )

    override fun getItemCount() = rooms.size

    override fun onBindViewHolder(
        holder: MSHolderRoom,
        position: Int
    ) {
        holder.model = rooms[position]
    }


}