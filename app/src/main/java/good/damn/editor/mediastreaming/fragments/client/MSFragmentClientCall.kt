package good.damn.editor.mediastreaming.fragments.client

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.MSActivityMain
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.MSStreamCameraInput
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.toast
import good.damn.editor.mediastreaming.network.client.tcp.MSClientConnectRoomTCP
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnConnectRoom
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnError
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFrame
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFramePiece
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.media.gles.GLViewTexture
import good.damn.media.gles.gl.textures.GLTextureBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress

class MSFragmentClientCall
: Fragment(),
MSListenerOnError,
MSListenerOnConnectRoom,
MSListenerOnReceiveFramePiece,
MSListenerOnResultPermission {

    companion object {
        private val TAG = MSFragmentClientCall::class
            .simpleName

        const val PREVIEW_WIDTH = 360
        const val PREVIEW_HEIGHT = 240
    }

    var rootFragment: MSFragmentClient? = null

    var roomId = -1
    var hostIp: String? = null

    private var managerCamera: MSManagerCamera? = null
    private var mStreamCamera: MSStreamCameraInput? = null

    private val mServerFrame = MSServerUDP(
        7777,
        61000,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverCameraFrame().apply {
            onReceiveFramePiece = this@MSFragmentClientCall
        }
    )

    private val mUsersMap = HashMap<Byte, MSModelCall>()

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

        managerCamera = MSManagerCamera(
            context
        )

        MSClientConnectRoomTCP(
            CoroutineScope(
                Dispatchers.IO
            )
        ).apply {
            onError = this@MSFragmentClientCall
            onConnectRoom = this@MSFragmentClientCall

            this.host = InetSocketAddress(
                hostIp,
                8081
            )

            connectToRoomAsync(
                roomId
            )
        }

        (activity as? MSActivityMain)
            ?.launcherPermission
            ?.launch(
                Manifest.permission.CAMERA
            )
    }

    override fun onStop() {
        super.onStop()
        mStreamCamera?.release()
        mServerFrame.release()
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

        context.toast(
            "Connected as $userId"
        )

        mServerFrame.start()

        mUserRoomId = userId

        (view as? ViewGroup)?.apply {
            users?.forEach {
                val texture = GLTextureBitmap(
                    PREVIEW_WIDTH,
                    PREVIEW_HEIGHT
                )

                val view = GLViewTexture(
                    context,
                    texture
                )

                addView(
                    view,
                    PREVIEW_WIDTH,
                    PREVIEW_HEIGHT
                )

                mUsersMap[
                    it.toByte()
                ] = MSModelCall(
                    texture,
                    view
                )
            }
        }
    }

    override suspend fun onReceiveFrame(
        userId: Byte,
        roomId: Byte,
        frame: Bitmap,
        rotation: Int
    ) {
        Log.d(TAG, "onReceiveFrame: $userId $roomId ${mUsersMap.size}")
        mUsersMap[
            userId
        ]?.apply {
            texture.bitmap = frame
            view.rotationShade = rotation

            withContext(
                Dispatchers.Main
            ) {
                view.requestRender()
            }
        }
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
                initCamera()
            }

            Manifest.permission.RECORD_AUDIO -> {

            }
        }

    }

    private inline fun initCamera() = managerCamera?.run {
        mStreamCamera = MSStreamCameraInput(
            this,
            roomId.toByte(),
            CoroutineScope(
                Dispatchers.IO
            ),
            mUserRoomId.toByte()
        ).apply {
            getCameraIds().firstOrNull()?.let {
                cameraId = MSCameraModelID(
                    it.logical,
                    it.physical
                )
            }

            start()
        }
    }

}

private data class MSModelCall(
    val texture: GLTextureBitmap,
    val view: GLViewTexture
)