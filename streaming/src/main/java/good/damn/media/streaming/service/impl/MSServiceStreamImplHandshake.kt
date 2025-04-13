package good.damn.media.streaming.service.impl

import android.util.Log
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.env.MSEnvironmentHandshake
import good.damn.media.streaming.models.handshake.MSMHandshakeAccept
import good.damn.media.streaming.models.handshake.MSMHandshakeSendInfo
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import kotlin.random.Random

class MSServiceStreamImplHandshake(
    private val userId: Int
): MSListenerOnHandshakeSettings {

    companion object {
        private const val TAG = "MSServiceStreamImplHand"
    }

    private val mHandshakes = HashMap<Int, MSMHandshakeSave>()

    private var mHandshake: MSEnvironmentHandshake? = null

    var onConnectUser: MSListenerOnConnectUser? = null
    var onSuccessHandshake: MSListenerOnSuccessHandshake? = null

    fun startCommand() {
        mHandshake = MSEnvironmentHandshake().apply {
            onHandshakeSettings = this@MSServiceStreamImplHandshake
        }
    }

    fun requestConnectedUsers() = mHandshakes.forEach {
        onConnectUser?.onConnectUser(
            MSMHandshakeAccept(
                it.value.settings,
                it.value.address,
                it.key,
                it.value.config
            )
        )
    }

    fun sendHandshakeSettings(
        model: MSMHandshakeSendInfo?
    ) = CoroutineScope(
        Dispatchers.IO
    ).launch {
        val result = mHandshake?.sendHandshakeSettings(
            userId,
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
        ] = MSMHandshakeSave(
            result.settings,
            result.address,
            result.config
        )

        withContext(
            Dispatchers.Main
        ) {
            onConnectUser?.onConnectUser(
                result
            )
        }
    }
}

private data class MSMHandshakeSave(
    val settings: MSTypeDecoderSettings,
    val address: InetAddress,
    val config: ByteArray
)