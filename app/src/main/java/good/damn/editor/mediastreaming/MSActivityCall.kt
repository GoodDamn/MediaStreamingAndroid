package good.damn.editor.mediastreaming

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.stream.MSStreamAudioInput
import good.damn.editor.mediastreaming.camera.MSManagerCamera
import good.damn.editor.mediastreaming.camera.MSStreamCameraInput
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import good.damn.editor.mediastreaming.extensions.toast
import good.damn.editor.mediastreaming.network.client.tcp.MSClientConnectRoomTCP
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnConnectRoom
import good.damn.editor.mediastreaming.network.client.tcp.listeners.MSListenerOnError
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFrame
import good.damn.editor.mediastreaming.network.server.MSReceiverCameraFrameRoom
import good.damn.editor.mediastreaming.network.server.MSServerUDP
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFramePiece
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission
import good.damn.media.gles.GLViewTexture
import good.damn.media.gles.gl.textures.GLTexture
import good.damn.media.gles.gl.textures.GLTextureBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress

class MSActivityCall
: AppCompatActivity(),
MSListenerOnError,
MSListenerOnConnectRoom,
MSListenerOnReceiveFramePiece,
MSListenerOnResultPermission {

    companion object {
        private val TAG = MSActivityCall::class
            .simpleName

        const val INTENT_KEY_ROOM_ID = "roomID"
        const val INTENT_KEY_ROOM_HOST = "host"

        const val PREVIEW_WIDTH = 360
        const val PREVIEW_HEIGHT = 240
    }

    private var managerCamera: MSManagerCamera? = null
    private var mStreamCamera: MSStreamCameraInput? = null

    private val mServerFrame = MSServerUDP(
        7777,
        61000,
        CoroutineScope(
            Dispatchers.IO
        ),
        MSReceiverCameraFrame().apply {
            onReceiveFramePiece = this@MSActivityCall
        }
    )

    private val mPermission = MSPermission().apply {
        onResultPermission = this@MSActivityCall
    }

    private val mUsersMap = HashMap<Byte, MSModelCall>()

    private var mRoomId = -1
    private var mUserRoomId = -1

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        managerCamera = MSManagerCamera(
            this
        )

        mRoomId = intent.getIntExtra(
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
                mRoomId
            )
        }

        mPermission.apply {
            register(
                this@MSActivityCall
            )
            launch(
                Manifest.permission.CAMERA
            )
        }

    }

    override fun onStop() {
        super.onStop()
        mStreamCamera?.release()
        mServerFrame.release()
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
        mUserRoomId = userId

        val context = this
        LinearLayout(
            context
        ).apply {
            orientation = LinearLayout
                .HORIZONTAL

            users?.forEach {
                val texture = GLTextureBitmap(
                    PREVIEW_WIDTH,
                    PREVIEW_HEIGHT
                )

                val view = GLViewTexture(
                    context,
                    texture
                )

                addView(view)

                mUsersMap[
                    it.toByte()
                ] = MSModelCall(
                    texture,
                    view
                )
            }

            setContentView(
                this
            )
        }
    }

    override suspend fun onReceiveFrame(
        userId: Byte,
        roomId: Byte,
        frame: Bitmap,
        rotation: Int
    ) {
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
            mRoomId.toByte(),
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