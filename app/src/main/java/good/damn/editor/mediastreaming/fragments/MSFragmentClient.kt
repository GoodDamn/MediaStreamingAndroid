package good.damn.editor.mediastreaming.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.network.client.tcp.MSClientGuildTCP
import good.damn.editor.mediastreaming.network.client.tcp.MSListenerOnGetRooms
import good.damn.editor.mediastreaming.network.client.tcp.MSModelRoomClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetSocketAddress

class MSFragmentClient
: Fragment(),
MSListenerOnGetRooms {

    companion object {
        private val TAG = MSFragmentClient::class.simpleName
        const val PREVIEW_WIDTH = 360
        const val PREVIEW_HEIGHT = 240
    }

    private var mEditTextHost: EditText? = null

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
                    onClickBtnAudioStream(this)
                }

                addView(
                    this,
                    -1,
                    -2
                )
            }

            layoutParams = FrameLayout.LayoutParams(
                -1,
                -1
            )

        }

        return root
    }

    private inline fun onClickBtnAudioStream(
        btn: Button
    ) {
        btn.text = "Connecting..."
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

    }
}