package good.damn.editor.mediastreaming.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.adapters.MSAdapterRooms
import good.damn.editor.mediastreaming.audio.MSRecordAudio
import good.damn.editor.mediastreaming.network.server.MSReceiverAudio
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFrameRoom
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.accepters.MSAccepterGetRoomList
import good.damn.editor.mediastreaming.network.server.guild.MSServerTCP
import good.damn.editor.mediastreaming.network.server.room.MSRoom
import good.damn.editor.mediastreaming.network.server.room.MSRoomUser
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import good.damn.editor.mediastreaming.system.MSServiceHotspotCompat
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.LinkedList

class MSFragmentServer
: Fragment(),
MSListenerOnGetHotspotHost {

    companion object {
        private val TAG = MSFragmentServer::class.simpleName
    }

    private val mRooms = MSRooms().apply {
        addRoom(
            MSRoom()
        )
    }

    private val mServerGuild = MSServerTCP(
        8080,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSAccepterGetRoomList(
            mRooms
        )
    )

    /*private val mServerAudio = MSServerUDP(
        5555,
        MSRecordAudio.DEFAULT_BUFFER_SIZE,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverAudio()
    )*/

    private val mServerFrameRoom = MSServerUDP(
        5556,
        61000,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverCameraFrameRoom(
            mRooms
        )
    )

    private var mTextViewIp: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context
            ?: return null

        MSServiceHotspotCompat(
            context
        ).apply {
            delegate = this@MSFragmentServer
            start()
        }

        return LinearLayout(
            context
        ).apply {
            orientation = LinearLayout
                .VERTICAL

            mTextViewIp = TextView(
                context
            ).apply {

                addView(
                    this,
                    -2,
                    -2
                )
            }

            Button(
                context
            ).apply {

                text = "Start server"

                setOnClickListener {
                    onClickBtnStartServer(this)
                }

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

                val list = ArrayList<MSRoom>(
                    mRooms.size
                )

                for (entry in mRooms.entries) {
                    list.add(
                        entry.value
                    )
                }

                it.adapter = MSAdapterRooms(
                    list
                )

                addView(it)
            }
        }
    }


    override fun onGetHotspotIP(
        addressList: String
    ) {
        MSApp.ui {
            mTextViewIp?.text = "Host: $addressList\n\n" +
                "Port: 5555 (Audio)\n" +
                "Port: 5556 (Camera)"
        }
    }

    override fun onStop() {
        mServerGuild.release()
        mServerFrameRoom.release()
        super.onStop()
    }

    private inline fun onClickBtnStartServer(
        btn: Button
    ) {
        mServerFrameRoom.apply {
            btn.text = if (isRunning) {
                stop()
                mServerGuild.stop()
                "Start server"
            } else {
                start()
                mServerGuild.start()
                "Stop Server"
            }
        }
    }

}