package good.damn.media.streaming.service

import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress

class MSServiceStreamImplHandshake
: MSListenerOnHandshakeSettings {

    private var mHandshake: MSEnvironmentHandshake? = null

    var onSuccessHandshake: MSListenerOnSuccessHandshake? = null

    fun startCommand() {
        mHandshake = MSEnvironmentHandshake().apply {
            onHandshakeSettings = this@MSServiceStreamImplHandshake
        }
    }

    fun sendHandshakeSettings(
        host: String,
        settings: MSTypeDecoderSettings
    ) = CoroutineScope(
        Dispatchers.IO
    ).launch {
        val result = mHandshake?.sendHandshakeSettings(
            host,
            settings
        )

        if (result == false) {
            return@launch
        }

        onSuccessHandshake?.onSuccessHandshake(
            MSMHandshake(1)
        )
    }

    fun startListeningSettings() {
        mHandshake?.startListeningSettings()
    }

    fun destroy() {
        mHandshake?.release()
    }

    override suspend fun onHandshakeSettings(
        settings: MSTypeDecoderSettings,
        fromIp: InetAddress
    ) {

    }
}