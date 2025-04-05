package good.damn.editor.mediastreaming

import android.media.MediaFormat
import android.os.Handler
import good.damn.editor.mediastreaming.system.service.MSServiceStreamWrapper
import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.avc.MSCoder
import good.damn.media.streaming.extensions.camera2.default
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.client.tcp.MSClientTCP
import good.damn.media.streaming.network.client.tcp.MSNetworkDecoderSettings
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import good.damn.media.streaming.network.server.tcp.MSServerTCP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress

class MSEnvironmentHandshake {

    var onHandshakeSettings: MSListenerOnHandshakeSettings?
        get() = mNetworkHandshakeSettings.onHandshakeSettings
        set(v) {
            mNetworkHandshakeSettings.onHandshakeSettings = v
        }

    private val mClient = MSClientTCP()

    private val mNetworkHandshakeSettings = MSNetworkDecoderSettings()

    private val mServerHandshakeSettings = MSServerTCP(
        mNetworkHandshakeSettings,
        CoroutineScope(
            Dispatchers.IO
        )
    )

    fun sendHandshakeSettings(
        host: String,
        settings: MSTypeDecoderSettings
    ) = try {
        mNetworkHandshakeSettings.sendDecoderSettings(
            InetSocketAddress(
                host,
                MSStreamConstants.PORT_HANDSHAKE
            ),
            mClient,
            settings
        )
    } catch (e: IOException) {
        false
    }

    fun startListeningSettings() {
        mServerHandshakeSettings.start(
            MSStreamConstants.PORT_HANDSHAKE
        )
    }

    fun stop() {
        mServerHandshakeSettings.stop()
    }

    fun release() {
        mServerHandshakeSettings.release()
    }
}