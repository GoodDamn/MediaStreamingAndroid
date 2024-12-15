package good.damn.editor.mediastreaming.holders

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import good.damn.editor.mediastreaming.network.server.room.MSRoom

class MSHolderRoom(
    private val textViewRoomName: TextView,
    private val textViewRoomUsers: TextView,
    view: View
): RecyclerView.ViewHolder(
    view
), View.OnClickListener {

    var model: MSRoom? = null
        set(v) {
            field = v
            v?.apply {
                textViewRoomName.text = toString()
                textViewRoomUsers.text = "Users - ${users.size}"
            }
        }

    override fun onClick(
        v: View?
    ) {

    }

    companion object {
        fun create(
            context: Context
        ) = LinearLayout(
            context
        ).run {
            orientation = LinearLayout
                .VERTICAL

            layoutParams = ViewGroup.LayoutParams(
                -1,
                -1
            )

            return@run MSHolderRoom(
                textViewRoomName = TextView(
                    context
                ).apply {
                    typeface = Typeface.DEFAULT_BOLD

                    text = "ROOM_NAME"

                    gravity = Gravity
                        .CENTER_HORIZONTAL

                    addView(
                        this,
                        -1,
                        -2
                    )
                },
                textViewRoomUsers = TextView(
                    context
                ).apply {

                    text = "USERS_COUNT"

                    gravity = Gravity
                        .CENTER_HORIZONTAL

                    isAllCaps = false

                    addView(
                        this,
                        -1,
                        -2
                    )
                },
                this
            ).apply {
                setOnClickListener(
                    this
                )
            }
        }
    }

}