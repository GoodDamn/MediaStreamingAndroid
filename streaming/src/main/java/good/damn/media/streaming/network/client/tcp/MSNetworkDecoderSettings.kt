package good.damn.media.streaming.network.client.tcp

import android.util.Log
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.extensions.integerBE
import good.damn.media.streaming.extensions.readU
import good.damn.media.streaming.extensions.toByteArray
import good.damn.media.streaming.network.server.listeners.MSListenerOnAcceptClient
import good.damn.media.streaming.network.server.listeners.MSListenerOnHandshakeSettings
import good.damn.media.streaming.service.MSMHandshakeResult
import good.damn.media.streaming.service.MSMHandshakeAccept
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

class MSNetworkDecoderSettings
: MSListenerOnAcceptClient {

    companion object {
        private const val TAG = "MSNetworkDecoderSetting"
        private const val ANSWER_OK = 0xA
        private val CHARSET_KEY = StandardCharsets.US_ASCII
    }

    var onHandshakeSettings: MSListenerOnHandshakeSettings? = null

    fun sendDecoderSettings(
        userId: Int,
        host: InetSocketAddress,
        client: MSClientTCP,
        settings: MSTypeDecoderSettings
    ) = client.connect(
        host
    )?.run {
        second.write(
            settings.size
        )

        Log.d(TAG, "sendDecoderSettings: SIZE: ${settings.size}")

        second.write(
            userId.toByteArray()
        )

        settings.forEach {
            val dataKey = it.key.toByteArray(
                CHARSET_KEY
            )

            second.write(
                dataKey.size
            )

            second.write(
                dataKey
            )

            second.write(
                it.value.toByteArray()
            )

            Log.d(TAG, "sendDecoderSettings: ${it.key}(${dataKey.size}) ${it.value}")
        }

        val isOk = first.read() == ANSWER_OK

        client.close()

        return@run MSMHandshakeResult(
            isOk,
            userId
        )
    }

    override suspend fun onAcceptClient(
        socket: Socket
    ) = socket.run {
        val srcAddr = socket.inetAddress

        val inp = socket.getInputStream()
        val out = socket.getOutputStream()

        val size = inp.readU()
        Log.d(TAG, "onAcceptClient: SIZE: $size")

        if (size < 0) {
            close()
            return@run
        }

        val bufferVal = ByteArray(4)
        if (inp.read(
            bufferVal,
            0,
            4
        ) < 0) {
            return@run
        }

        val userId = bufferVal.integerBE(0)

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

            Log.d(TAG, "onAcceptClient: KEY($dataKeySize): $key")

            if (inp.read(
                bufferVal,
                0,
                4
            ) < 0) {
                continue
            }

            val value = bufferVal.integerBE(0)

            Log.d(TAG, "onAcceptClient: VALUE: $value")

            map[key] = value
        }

        out.write(ANSWER_OK)

        onHandshakeSettings?.onHandshakeSettings(
            MSMHandshakeAccept(
                map,
                srcAddr,
                userId
            )
        )
    }

}