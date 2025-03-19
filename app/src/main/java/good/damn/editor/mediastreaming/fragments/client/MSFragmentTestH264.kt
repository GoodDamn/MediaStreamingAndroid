package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.extensions.hasPermissionCamera
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.views.MSViewStreamFrame
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.editor.mediastreaming.views.MSListenerOnChangeSurface
import good.damn.media.streaming.camera.avc.cache.MSListenerOnOrderPacket
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.network.server.udp.MSPacketMissingHandler
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrame
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress

class MSFragmentTestH264
: Fragment(),
MSListenerOnResultPermission,
MSListenerOnSelectCamera,
MSListenerOnChangeSurface {

    companion object {
        private const val TAG = "MSFragmentTestH264"
        private val RESOLUTION = Size(
            640,
            480
        )
    }

    private var mEditTextHost: EditText? = null

    private val mReceiverFrame = MSReceiverCameraFrame()
    private val mServiceStreamWrapper = MSServiceStreamWrapper()
    private val mHandlerPacketMissing = MSPacketMissingHandler()

    private val mBufferizerRemote = MSPacketBufferizer().apply {
        onGetOrderedFrame = mReceiverFrame
        mReceiverFrame.bufferizer = this
        mHandlerPacketMissing.bufferizer = this
    }

    private val mServerUDP = MSServerUDP(
        6666,
        MSStreamCameraInput.PACKET_MAX_SIZE + MSUtilsAvc.LEN_META,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverFrame
    )

    private val mServerRestorePackets = MSServerUDP(
        6667,
        MSStreamCameraInput.PACKET_MAX_SIZE + MSUtilsAvc.LEN_META,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverFrame
    )

    private var mSurfaceReceive: Surface? = null

    override fun onPause() {
        super.onPause()
        if (mServerUDP.isRunning) {
            mReceiverFrame.stop()
            mServerUDP.stop()
            mServerRestorePackets.stop()
            mHandlerPacketMissing.isRunning = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mReceiverFrame.release()
        mServerUDP.release()
        mServerRestorePackets.release()

        context?.apply {
            mServiceStreamWrapper.destroy(
                this
            )
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        mServiceStreamWrapper.start(
            requireActivity().applicationContext
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LinearLayout(
        context
    ).apply {

        orientation = LinearLayout
            .VERTICAL

        EditText(
            context
        ).apply {

            mEditTextHost = this

            setText(
                "127.0.0.1"
            )

            addView(
                this,
                -1,
                -2
            )
        }

        Button(
            context
        ).apply {

            text = "Start receiving"

            setOnClickListener {
                if (mServerUDP.isRunning) {
                    text = "Start receiving"
                    mReceiverFrame.stop()
                    mServerUDP.stop()
                    mServerRestorePackets.stop()
                    mHandlerPacketMissing.isRunning = false
                    return@setOnClickListener
                }

                val ip = mEditTextHost?.text?.toString()
                    ?: return@setOnClickListener

                val inet = InetAddress.getByName(ip)

                text = "Stop receiving"
                mSurfaceReceive?.apply {
                    mReceiverFrame.configure(
                        this,
                        MediaFormat.createVideoFormat(
                            MSCoder.TYPE_AVC,
                            RESOLUTION.width,
                            RESOLUTION.height
                        ).apply {
                            setInteger(
                                MediaFormat.KEY_ROTATION,
                                90
                            )
                        }
                    )

                    // Bufferizing
                    mServerUDP.start()
                    mServerRestorePackets.start()


                    mBufferizerRemote.onOrderPacket = MSListenerOnOrderPacket {
                        Log.d(TAG, "onCreateView: MSListenerOnOrderPacket: $it")
                        if (it > 15 && !mHandlerPacketMissing.isRunning) {
                            // Checking
                            mHandlerPacketMissing.handlingMissedPackets(
                                inet
                            )
                        }
                        
                        if (it > 30 && !mReceiverFrame.isDecoding) {
                            // Decoding
                            mReceiverFrame.startDecoding()
                            mBufferizerRemote.onOrderPacket = null;
                        }
                        
                    }
                    
                    CoroutineScope(
                        Dispatchers.IO
                    ).launch {
                        while (mServerUDP.isRunning) {
                            mBufferizerRemote.orderPacket()
                        }

                        mBufferizerRemote.clear()
                    }
                }
            }

            addView(
                this,
                -1,
                -2
            )
        }

        FrameLayout(
            context
        ).let {
            setBackgroundColor(0)

            MSViewStreamFrame(
                context
            ).apply {

                onChangeSurface = this@MSFragmentTestH264

                layoutParams = ViewGroup.LayoutParams(
                    MSApp.width,
                    (RESOLUTION.width.toFloat() / RESOLUTION.height * MSApp.width).toInt()
                )
                it.addView(
                    this
                )
            }

            LinearLayout(
                context
            ).apply {

                orientation = LinearLayout
                    .VERTICAL

                MSManagerCamera(
                    context
                ).getCameraIds().forEach {
                    addView(
                        Button(
                            context
                        ).apply {
                            text = "${it.logical}_${it.physical ?: ""}"
                            setOnClickListener(
                                MSClickOnSelectCamera(
                                    it
                                ).apply {
                                    onSelectCamera = this@MSFragmentTestH264
                                }
                            )
                        },
                        (0.15f * MSApp.width).toInt(),
                        -2
                    )
                }


                it.addView(
                    this,
                    -1,
                    -2
                )
            }

            addView(
                it
            )
        }
    }


    override fun onResultPermission(
        permission: String,
        result: Boolean
    ) = Unit

    override fun onSelectCamera(
        cameraId: MSCameraModelID
    ) {
        val activity = activity as? MSActivityMain
            ?: return

        if (!activity.hasPermissionCamera()) {
            activity.launcherPermission.launch(
                Manifest.permission.CAMERA
            )
            return
        }

        val ip = mEditTextHost?.text?.toString()
            ?: return

        mServiceStreamWrapper.serviceConnectionStream.binder?.apply {
            if (isStreaming) {
                stopStreaming()
            }

            startStreaming(
                cameraId.logical,
                cameraId.physical,
                ip,
                RESOLUTION.width,
                RESOLUTION.height
            )
        }

    }


    override fun onChangeSurface(
        surface: Surface
    ) {
        mSurfaceReceive = surface
    }

}