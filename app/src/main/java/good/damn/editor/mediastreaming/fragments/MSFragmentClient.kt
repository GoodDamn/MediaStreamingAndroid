package good.damn.editor.mediastreaming.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import good.damn.editor.mediastreaming.MSActivityCall
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.adapters.MSAdapterRoomsClient
import good.damn.editor.mediastreaming.adapters.listeners.MSListenerOnClickRoom
import good.damn.editor.mediastreaming.network.client.tcp.MSClientGuildTCP
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnGetRooms
import good.damn.editor.mediastreaming.network.client.tcp.MSModelRoomClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetSocketAddress

class MSFragmentClient
: Fragment(),
    MSListenerOnGetRooms,
MSListenerOnClickRoom {

    companion object {
        private val TAG = MSFragmentClient::class.simpleName
        const val PREVIEW_WIDTH = 360
        const val PREVIEW_HEIGHT = 240
    }

    private var mEditTextHost: EditText? = null
    private var mBtnConnect: Button? = null
    private var mAdapterRooms: MSAdapterRoomsClient? = null

    private val mClientGuild = MSClientGuildTCP(
        CoroutineScope(
            Dispatchers.IO
        )
    ).apply {
        onGetRooms = this@MSFragmentClient
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val context = context
            ?: return null

        mEditTextHost = EditText(
            context
        ).apply {
            hint = "Host"
            setText(
                "127.0.0.1"
            )
        }

        val root = LinearLayout(
            context
        ).apply {

            orientation = LinearLayout
                .VERTICAL

            addView(
                mEditTextHost,
                -1,
                -2
            )

            Button(
                context
            ).apply {
                text = "Connect to server"

                setOnClickListener {
                    onClickBtnAudioStream()
                }

                mBtnConnect = this

                addView(
                    this,
                    -1,
                    -2
                )
            }

            RecyclerView(
                context
            ).let {

                it.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )

                mAdapterRooms = MSAdapterRoomsClient().apply {
                    onClickRoom = this@MSFragmentClient
                }
                it.adapter = mAdapterRooms

                addView(
                    it,
                    -1,
                    -1
                )
            }

            layoutParams = FrameLayout.LayoutParams(
                -1,
                -1
            )

        }

        return root
    }

    private inline fun onClickBtnAudioStream() {
        mBtnConnect?.apply {
            text = "Connecting..."
            isEnabled = false
        }
        mEditTextHost?.isEnabled = false
        mClientGuild.apply {
            host = InetSocketAddress(
                mEditTextHost?.text?.toString(),
                8080
            )
            getRoomsAsync()
        }
    }

    override suspend fun onGetRooms(
        rooms: Array<MSModelRoomClient>
    ) {
        mBtnConnect?.text = "Connected"
        mEditTextHost?.isEnabled = false
        mAdapterRooms?.rooms = rooms
    }

    override fun onClickRoom(
        room: MSModelRoomClient
    ) {
        (activity as? MSActivityMain)?.apply {
            startActivity(
                Intent(
                    this,
                    MSActivityCall::class.java
                ).apply {
                    putExtra(
                        MSActivityCall.INTENT_KEY_ROOM_ID,
                        room.id
                    )

                    putExtra(
                        MSActivityCall.INTENT_KEY_ROOM_HOST,
                        mEditTextHost?.text?.toString()
                    )
                }
            )
        }
    }
}