package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.color.utilities.DislikeAnalyzer
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.audio.MSRecordAudio
import good.damn.editor.mediastreaming.audio.stream.MSStreamAudioInput
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.MSStreamCameraInput
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.toast
import good.damn.editor.mediastreaming.network.client.tcp.MSClientConnectRoomTCP
import good.damn.editor.mediastreaming.network.client.tcp.accepters.MSAccepterGetNewUserClient
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnAcceptNewUser
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnConnectRoom
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnError
import good.damn.editor.mediastreaming.network.server.MSReceiverAudio
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFrame
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.guild.MSServerTCP
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFramePiece
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.media.gles.GLViewTexture
import good.damn.media.gles.gl.textures.GLTextureBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class MSFragmentClientCall
: Fragment(),
MSListenerOnError,
MSListenerOnConnectRoom,
MSListenerOnReceiveFramePiece,
MSListenerOnResultPermission, MSListenerOnAcceptNewUser {

    companion object {
        private val TAG = MSFragmentClientCall::class
            .simpleName

        const val PREVIEW_WIDTH = 360
        const val PREVIEW_HEIGHT = 240
    }

    var rootFragment: MSFragmentClient? = null

    var roomId = -1
    var hostIp: String? = null

    private var mStreamAudio: MSStreamAudioInput? = null

    private val mServerNewUser = MSServerTCP(
        7780,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSAccepterGetNewUserClient().apply {
            onAcceptNewUser = this@MSFragmentClientCall
        }
    )

    private val mServerAudio = MSServerUDP(
        7777,
        MSReceiverAudio.BUFFER_SIZE,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverAudio()
    )

    private val mUsersMap = ConcurrentHashMap<Byte, MSModelCall>()

    private var mUserRoomId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context
            ?: return null

        return LinearLayout(
            context
        ).apply {
            orientation = LinearLayout.VERTICAL
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = context
            ?: return

        MSClientConnectRoomTCP(
            CoroutineScope(
                Dispatchers.IO
            )
        ).apply {
            onError = this@MSFragmentClientCall
            onConnectRoom = this@MSFragmentClientCall

            this.host = InetSocketAddress(
                hostIp,
                8080
            )

            connectToRoomAsync(
                roomId
            )
        }

        (activity as? MSActivityMain)
            ?.launcherPermission
            ?.launch(
                Manifest.permission.RECORD_AUDIO
            )
    }

    override fun onStop() {
        super.onStop()
        mStreamAudio?.release()
        mServerAudio.release()
        mServerNewUser.release()
    }

    override suspend fun onError(
        msg: String
    ) = context?.toast(msg) ?: Unit

    override suspend fun onConnectRoom(
        userId: Int,
        users: Array<Int>?
    ) {
        val context = context
            ?: return

        mUserRoomId = userId

        context.toast(
            "Connected as $userId"
        )

        mServerAudio.start()
        mServerNewUser.start()

        mStreamAudio?.apply {
            roomId = this@MSFragmentClientCall.roomId.toByte()
            this.userId = mUserRoomId.toByte()
        }

        users?.forEach {
            createNewUserView(
                it.toByte()
            )
        }
    }

    override suspend fun onReceiveFrame(
        userId: Byte,
        roomId: Byte,
        frame: Bitmap,
        rotation: Int
    ) {
//        Log.d(TAG, "onReceiveFrame: ${mUserRoomId.toByte()} $userId;;;; ${this.roomId.toByte()} $roomId")
//        mUsersMap[
//            userId
//        ]?.apply {
//            Log.d(TAG, "onReceiveFrame: BITMAP: $frame")
//            texture.bitmap = frame
//            view.rotationShade = rotation
//
//            withContext(
//                Dispatchers.Main
//            ) {
//                view.requestRender()
//            }
//        }
    }

    override fun onResultPermission(
        permission: String,
        result: Boolean
    ) {
        if (!result) {
            return
        }

        when (permission) {
            Manifest.permission.CAMERA -> {
                //initCamera()
            }

            Manifest.permission.RECORD_AUDIO -> {
                initMicrophone()
            }
        }

    }

    override fun onAcceptNewUser(
        userId: Int
    ) {
        Handler(
            Looper.getMainLooper()
        ).post {
            createNewUserView(
                userId.toByte()
            )
        }
    }

    private inline fun createNewUserView(
        userId: Byte
    ) {
        val context = context
            ?: return

        val layout = FrameLayout(
            context
        ).apply {
            setBackgroundColor(
                Random.nextInt()
            )
        }

        mUsersMap[
            userId
        ] = MSModelCall(
            layout
        )

        (view as? ViewGroup)?.addView(
            layout,
            (MSApp.width * 0.3f).toInt(),
            (MSApp.height * 0.3f).toInt()
        )
    }

    private inline fun initMicrophone() {
        mStreamAudio = MSStreamAudioInput().apply {
            roomId = this@MSFragmentClientCall.roomId.toByte()
            userId = mUserRoomId.toByte()
            start()
        }
    }

/*
    private inline fun initCamera() = managerCamera?.run {
        mStreamCamera = MSStreamCameraInput(
            this,
            CoroutineScope(
                Dispatchers.IO
            )
        ).apply {
            roomId = this@MSFragmentClientCall.roomId.toByte()
            userId = mUserRoomId.toByte()

            getCameraIds().firstOrNull()?.let {
                cameraId = MSCameraModelID(
                    it.logical,
                    it.physical
                )
            }

            start()
        }
    }*/

}

private data class MSModelCall(
    val view: FrameLayout
)