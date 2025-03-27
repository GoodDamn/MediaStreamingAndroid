package good.damn.editor.mediastreaming.system.service

import android.os.Binder
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriberUDP
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import java.net.InetAddress

class MSServiceStreamBinder(
    private val managerCamera: MSManagerCamera,
    private val mSubscriber: MSStreamSubscriberUDP,
    private val mStreamCamera: MSStreamCameraInput,
    private val mServerRestore: MSServerUDP,
    private val mServerAudio: MSServerUDP,
    private val mReceiverCameraFrameRestore: MSReceiverCameraFrameRestore
): Binder() {

    val isStreamingAudio: Boolean
        get() = mServerAudio.isRunning

    val isStreamingCamera: Boolean
        get() = mStreamCamera.isRunning

    fun stopStreamingAudio() {
        mServerAudio.stop()
    }

    fun stopStreamingCamera() {
        mSubscriber.stop()
        mStreamCamera.stop()
        mServerRestore.stop()
    }

    fun startStreaming(
        idLogical: String,
        idPhysical: String?,
        host: String,
        width: Int,
        height: Int
    ) {
        mSubscriber.host = InetAddress.getByName(
            host
        )
        mReceiverCameraFrameRestore.host = mSubscriber.host

        mSubscriber.start()
        mServerRestore.start()

        mServerAudio.start()

        mStreamCamera.start(
            MSCameraModelID(
                idLogical,
                idPhysical,
                characteristics = managerCamera.getCharacteristics(
                    idPhysical ?: idLogical
                )
            ),
            width,
            height
        )
    }
}