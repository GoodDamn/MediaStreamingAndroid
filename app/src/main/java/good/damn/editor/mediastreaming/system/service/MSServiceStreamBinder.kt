package good.damn.editor.mediastreaming.system.service

import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import good.damn.media.streaming.audio.stream.MSStreamAudioInput
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriberUDP
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import java.net.InetAddress

class MSServiceStreamBinder(
    private val managerCamera: MSManagerCamera,
    private val mSubscriber: MSStreamSubscriberUDP,
    private val mStreamCamera: MSStreamCameraInput,
    private val mStreamAudio: MSStreamAudioInput,
    private val mServerRestore: MSServerUDP,
    private val mReceiverCameraFrameRestore: MSReceiverCameraFrameRestore,
    private val mHandler: Handler
): Binder() {

    val isStreamingAudio: Boolean
        get() = mStreamAudio.isRunning

    val isStreamingCamera: Boolean
        get() = mStreamCamera.isRunning

    fun stopStreamingAudio() {
        mStreamAudio.stop()
    }

    fun stopStreamingCamera() {
        mStreamCamera.stop()
        mServerRestore.stop()
    }

    fun startStreamingAudio(
        host: String,
    ) {
        mStreamAudio.start(
            host.toInetAddress(),
            mHandler
        )
    }

    fun startStreamingVideo(
        idLogical: String,
        idPhysical: String?,
        host: String,
        width: Int,
        height: Int
    ) {
        mSubscriber.host = host.toInetAddress()
        mReceiverCameraFrameRestore.host = mSubscriber.host

        mServerRestore.start()

        mStreamCamera.start(
            MSCameraModelID(
                idLogical,
                idPhysical,
                characteristics = managerCamera.getCharacteristics(
                    idPhysical ?: idLogical
                )
            ),
            width,
            height,
            mHandler
        )
    }
}