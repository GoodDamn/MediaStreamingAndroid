package good.damn.media.streaming.network.client.tcp

import android.util.Log
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.extensions.integerBE
import good.damn.media.streaming.extensions.integerLE
import good.damn.media.streaming.extensions.readU
import good.damn.media.streaming.extensions.write
import good.damn.media.streaming.network.server.listeners.MSListenerOnAcceptClient
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

class MSNetworkDecoderSettings
: MSListenerOnAcceptClient {

    companion object {
        private const val TAG = "MSNetworkDecoderSetting"
        private const val ANSWER_OK = 0xA
        private val CHARSET_KEY = StandardCharsets.UTF_8
    }

    var onHandshakeSettings: MSListenerOnHandshakeSettings? = null

    fun sendDecoderSettings(
        host: InetSocketAddress,
        client: MSClientTCP,
        settings: MSTypeDecoderSettings
    ) = client.connect(
        host
    )?.run {
        second.write(
            settings.size
        )

        var dataKey: ByteArray
        settings.forEach {
            dataKey = it.key.toByteArray(
                CHARSET_KEY
            )

            second.write(
                dataKey.size
            )

            second.write(
                dataKey
            )

            it.value.write(
                second
            )
        }


        if (first.read() == ANSWER_OK) {
            first.close()
            second.close()
            client.close()
            return@run true
        }

        return@run false
    } ?: false

    override suspend fun onAcceptClient(
        socket: Socket
    ) = socket.run {
        val inp = socket.getInputStream()
        val out = socket.getOutputStream()

        val size = inp.readU()

        if (size < 0) {
            close()
            return@run
        }

        val map = HashMap<String, Int>(
            size
        )

        var dataKeySize: Int
        val buffer = ByteArray(512)

        for (i in 0 until size) {
            dataKeySize = inp.readU()

            if (dataKeySize < 0) {
                continue
            }

            if (inp.read(
                buffer,
                0,
                dataKeySize
            ) < 0) {
                continue
            }

            val key = String(
                buffer,
                0,
                dataKeySize,
                CHARSET_KEY
            )

            if (inp.read(
                buffer,
                0,
                4
            ) < 0) {
                continue
            }

            val value = buffer.integerLE(0)

            Log.d(TAG, "onAcceptClient: $key:$value")

            map[key] = value
        }

        out.write(ANSWER_OK)

        onHandshakeSettings?.onHandshakeSettings(
            map,
            socket.inetAddress
        )
    }

}