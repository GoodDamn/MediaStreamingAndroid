package good.damn.editor.mediastreaming.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import good.damn.editor.mediastreaming.adapters.listeners.MSListenerOnClickRoom
import good.damn.editor.mediastreaming.holders.MSHolderRoom
import good.damn.editor.mediastreaming.holders.MSHolderRoomClient
import good.damn.editor.mediastreaming.network.client.tcp.MSModelRoomClient
import good.damn.editor.mediastreaming.network.server.room.MSRoom
import good.damn.editor.mediastreaming.network.server.room.MSRooms

class MSAdapterRoomsClient
: RecyclerView.Adapter<
    MSHolderRoomClient
>() {

    var onClickRoom: MSListenerOnClickRoom? = null

    var rooms: Array<MSModelRoomClient>? = null
        set(v) {
            field = v
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = MSHolderRoomClient.create(
        parent.context
    )

    override fun getItemCount() = rooms?.size ?: 0

    override fun onBindViewHolder(
        holder: MSHolderRoomClient,
        position: Int
    ) {
        holder.onClickRoom = onClickRoom
        holder.model = rooms?.get(
            position
        )
    }


}