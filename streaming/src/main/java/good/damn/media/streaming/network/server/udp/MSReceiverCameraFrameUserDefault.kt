package good.damn.media.streaming.network.server.udp

import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import good.damn.media.streaming.MSEnvironmentVideoDecodeStream
import java.net.InetAddress

class MSReceiverCameraFrameUserDefault(
    override val surfaceView: SurfaceView
): MSIReceiverCameraFrameUser {

    companion object {
        private const val TAG = "MSReceiverCameraFrameUs"
    }

    private val mEnvDecode = MSEnvironmentVideoDecodeStream()

    override fun receiveUserFrame(
        data: ByteArray
    ) {
        mEnvDecode.writeToBuffer(
            data
        )
    }

    override fun startReceive(
        userId: Int,
        surfaceOutput: Surface,
        format: MediaFormat,
        host: InetAddress?
    ) {
        mEnvDecode.start(
            userId,
            surfaceOutput,
            format,
            host
        )
    }

    override fun stop() {
        mEnvDecode.stop()
    }

    override fun release() {
        mEnvDecode.release()
    }

}