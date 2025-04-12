package good.damn.media.streaming.service

import good.damn.media.streaming.MSStreamConstants
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.network.client.tcp.MSClientTCP
import good.damn.media.streaming.network.client.tcp.MSNetworkDecoderSettings
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import good.damn.media.streaming.network.server.tcp.MSServerTCP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.IOException
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
        userId: Int,
        model: MSMHandshakeSendInfo
    ) = try {
        mNetworkHandshakeSettings.sendDecoderSettings(
            userId,
            InetSocketAddress(
                model.host,
                MSStreamConstants.PORT_HANDSHAKE
            ),
            mClient,
            model.settings
        )
    } catch (e: IOException) {
        null
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