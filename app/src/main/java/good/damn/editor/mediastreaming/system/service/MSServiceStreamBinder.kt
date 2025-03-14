package good.damn.editor.mediastreaming.system.service

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import good.damn.editor.mediastreaming.system.service.MSServiceStream.Companion.EXTRA_HOST
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.MSStreamSubscriberUDP
import good.damn.media.streaming.camera.models.MSCameraModelID
import java.io.FileDescriptor
import java.net.InetAddress

class MSServiceStreamBinder(
    private val managerCamera: MSManagerCamera,
    private val mSubscriber: MSStreamSubscriberUDP,
    private val mStreamCamera: MSStreamCameraInput
): Binder() {

    val isStreaming: Boolean
        get() = mStreamCamera.isRunning

    fun stopStreaming() {
        mSubscriber.stop()
        mStreamCamera.stop()
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

        mSubscriber.start()

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