package good.damn.media.streaming.service

import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import kotlin.random.Random

class MSServiceStreamImplHandshake
: MSListenerOnHandshakeSettings {

    private val mHandshakes = HashMap<Int, MSTypeDecoderSettings>()

    private val mUserId = Random.nextInt()

    private var mHandshake: MSEnvironmentHandshake? = null

    var onConnectUser: MSListenerOnConnectUser? = null
    var onSuccessHandshake: MSListenerOnSuccessHandshake? = null

    fun startCommand() {
        mHandshake = MSEnvironmentHandshake().apply {
            onHandshakeSettings = this@MSServiceStreamImplHandshake
        }
    }

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo
    ) = CoroutineScope(
        Dispatchers.IO
    ).launch {
        val result = mHandshake?.sendHandshakeSettings(
            mUserId,
            model
        )

        onSuccessHandshake?.onSuccessHandshake(
            result
        )
    }

    fun startListeningSettings() {
        mHandshake?.startListeningSettings()
    }

    fun destroy() {
        mHandshake?.release()
    }

    override suspend fun onHandshakeSettings(
        result: MSMHandshakeAccept
    ) {
        mHandshakes[
            result.userId
        ] = result.settings

        withContext(
            Dispatchers.Main
        ) {
            onConnectUser?.onConnectUser(
                result
            )
        }
    }
}