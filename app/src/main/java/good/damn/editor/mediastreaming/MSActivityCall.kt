package good.damn.editor.mediastreaming

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.extensions.toast
import good.damn.editor.mediastreaming.network.client.tcp.MSClientConnectRoomTCP
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnConnectRoom
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.InetSocketAddress

class MSActivityCall
: AppCompatActivity(),
MSListenerOnError,
MSListenerOnConnectRoom {

    companion object {
        private val TAG = MSActivityCall::class
            .simpleName

        const val INTENT_KEY_ROOM_ID = "roomID"
        const val INTENT_KEY_ROOM_HOST = "host"
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val roomId = intent.getIntExtra(
            INTENT_KEY_ROOM_ID,
            -1
        )

        val host = intent.getStringExtra(
            INTENT_KEY_ROOM_HOST
        ) ?: return

        MSClientConnectRoomTCP(
            CoroutineScope(
                Dispatchers.IO
            )
        ).apply {
            onError = this@MSActivityCall
            onConnectRoom = this@MSActivityCall

            this.host = InetSocketAddress(
                host,
                8081
            )

            connectToRoomAsync(
                roomId
            )
        }

    }

    override suspend fun onError(
        msg: String
    ) = toast(msg)

    override suspend fun onConnectRoom(
        userId: Int,
        users: Array<Int>?
    ) {
        toast(
            "Connected as $userId"
        )

    }

}