package good.damn.editor.mediastreaming.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
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
import good.damn.editor.mediastreaming.network.server.MSReceiverAudioRoom
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFrame
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.accepters.MSAccepterGuild
import good.damn.editor.mediastreaming.network.server.guild.MSServerTCP
import good.damn.editor.mediastreaming.network.server.room.MSRoom
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import good.damn.editor.mediastreaming.system.MSServiceHotspotCompat
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSFragmentServer
: Fragment(),
MSListenerOnGetHotspotHost {

    companion object {
        private val TAG = MSFragmentServer::class.simpleName
    }

    private val mReceiverFrame = MSReceiverCameraFrame()

    private val mServerFrame = MSServerUDP(
        5556,
        1034,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverFrame
    )

    private var mTextViewIp: TextView? = null
    private var mLayoutRoot: LinearLayout? = null

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

            mLayoutRoot = this

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

    override fun onDestroy() {
        mReceiverFrame.release()
        mServerFrame.release()
        super.onDestroy()
    }

    private inline fun onClickBtnStartServer(
        btn: Button
    ) {
        mServerFrame.apply {
            btn.text = if (
                isRunning
            ) {
                mLayoutRoot?.apply {
                    removeViewAt(
                        childCount - 1
                    )
                }
                mReceiverFrame.stop()
                stop()
                "Start server"
            } else {
                mLayoutRoot?.addView(
                    SurfaceView(
                        context
                    ).apply {
                        post {
                            mReceiverFrame.configure(
                                holder.surface,
                                640,
                                480,
                                rotation = 90
                            )
                            mReceiverFrame.start()
                            start()
                        }
                    }
                )
                "Stop Server"
            }
        }
    }

}