package good.damn.media.streaming.env

import android.os.Handler
import android.os.HandlerThread
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.network.server.udp.MSIReceiverCameraFrameUser
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrame
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSEnvironmentGroupStream {

    var handler: Handler? = null
        private set

    private val mUsers = HashMap<
        Int,
        MSIReceiverCameraFrameUser
    >(30)

    private val mReceiverFrame = MSReceiverCameraFrame().apply {
        users = mUsers
    }

    private val mServerVideo = MSServerUDP(
        MSStreamConstants.PORT_MEDIA,
        MSStreamConstants.PACKET_MAX_SIZE,
        CoroutineScope(
            Dispatchers.IO
        ),
        mReceiverFrame
    )

    private var mThreadDecoding: HandlerThread? = null

    fun getUser(
        userId: Int
    ) = mUsers[userId]

    fun putUser(
        userId: Int,
        user: MSIReceiverCameraFrameUser
    ) {
        mUsers[userId] = user
    }

    fun removeUser(
        userId: Int
    ) {
        mUsers.remove(userId)
    }

    fun startReceivingFrames() {
        mServerVideo.start()
        mThreadDecoding = HandlerThread(
            "decodingGroupStream"
        ).apply {
            start()

            handler = Handler(
                looper
            )
        }
    }

    fun stop() {
        mThreadDecoding?.quit()
        mThreadDecoding = null

        handler = null

        mServerVideo.stop()
        mUsers.forEach {
            it.value.release()
        }
        mUsers.clear()
    }

    fun release() {
        stop()
    }

}