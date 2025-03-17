package good.damn.media.streaming.network.server

import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.Surface
import good.damn.media.streaming.camera.avc.MSDecoderAvc
import good.damn.media.streaming.camera.avc.MSUtilsAvc
import good.damn.media.streaming.camera.avc.cache.MSFrame
import good.damn.media.streaming.camera.avc.cache.MSIOnEachMissedPacket
import good.damn.media.streaming.camera.avc.cache.MSListenerOnGetOrderedFrame
import good.damn.media.streaming.camera.avc.cache.MSPacketBufferizer
import good.damn.media.streaming.extensions.integer
import good.damn.media.streaming.extensions.setIntegerOnPosition
import good.damn.media.streaming.extensions.setShortOnPosition
import good.damn.media.streaming.extensions.short
import good.damn.media.streaming.extensions.writeDefault
import good.damn.media.streaming.network.server.listeners.MSListenerOnReceiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

class MSReceiverCameraFrame
: MSListenerOnReceiveData,
MSListenerOnGetOrderedFrame {

    companion object {
        private const val TAG = "MSReceiverCameraFramePi"
    }

    private val mDecoder = MSDecoderAvc()
    private val mBufferizer = MSPacketBufferizer().apply {
        onGetOrderedFrame = this@MSReceiverCameraFrame
    }

    private val mScope = CoroutineScope(
        Dispatchers.IO
    )

    fun configure(
        decodeSurface: Surface,
        format: MediaFormat
    ) {
        mDecoder.configure(
            decodeSurface,
            format
        )
    }

    fun start() {
        mScope.launch {
            while (mDecoder.isRunning) {
                mBufferizer.orderPacket()
            }

            mBufferizer.clear()
        }

        mDecoder.start()

        /*Handler(
            Looper.getMainLooper()
        ).apply {

            postDelayed({

            }, 1500)

            postDelayed({

            }, 3500)
        }*/
    }

    fun stop() {
        mDecoder.stop()
        mScope.cancel()
    }

    fun release() {
        mDecoder.release()
    }

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        mBufferizer.writeDefault(
            data
        )
    }

    override fun onGetOrderedFrame(
        frame: MSFrame
    ) {
        mDecoder.addOrderedFrame(
            frame
        )
    }
}