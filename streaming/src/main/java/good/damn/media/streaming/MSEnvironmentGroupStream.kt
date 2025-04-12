package good.damn.media.streaming

import good.damn.media.streaming.network.server.udp.MSIReceiverCameraFrameUser
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrame
import good.damn.media.streaming.network.server.udp.MSServerUDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MSEnvironmentGroupStream {

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

    fun start() {
        mServerVideo.start()
    }

    fun stop() {
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