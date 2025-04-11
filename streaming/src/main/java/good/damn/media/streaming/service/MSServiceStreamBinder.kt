package good.damn.media.streaming.service

import android.media.MediaFormat
import android.os.Binder
import android.os.Handler
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.client.MSClientUDP
import good.damn.media.streaming.network.server.udp.MSServerUDP

class MSServiceStreamBinder(
    private var mStreamClient: MSClientUDP?,
    private var mStreamCamera: MSStreamCameraInput?,
    private var mServerRestorePackets: MSServerUDP?,
    private var mHandler: Handler?
): Binder() {

    fun startStreamingCamera(
        modelID: MSCameraModelID,
        mediaFormat: MediaFormat,
        host: String
    ) {
        mStreamClient?.host = host.toInetAddress()
        mServerRestorePackets?.start()
        mStreamCamera?.start(
            modelID,
            mediaFormat,
            mHandler!!
        )
    }

    fun stopStreamingCamera() {
        mServerRestorePackets?.stop()
        mStreamCamera?.stop()
    }

}