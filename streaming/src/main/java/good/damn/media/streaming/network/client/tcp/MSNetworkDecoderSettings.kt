package good.damn.media.streaming.network.client.tcp

import good.damn.media.streaming.extensions.integer
import good.damn.media.streaming.extensions.readU
import good.damn.media.streaming.extensions.write
import good.damn.media.streaming.network.server.listeners.MSListenerOnAcceptClient
import kotlinx.coroutines.CoroutineScope
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

class MSNetworkDecoderSettings
: MSListenerOnAcceptClient {

    companion object {
        private const val ANSWER_OK = 0xA
        private val CHARSET_KEY = StandardCharsets.US_ASCII
    }

    fun sendDecoderSettings(
        host: InetSocketAddress,
        client: MSClientTCP,
        settings: Map<String, Int>
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
            client.close()
        }
    }

    override fun onAcceptClient(
        socket: Socket,
        scope: CoroutineScope
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

        var dataKey: ByteArray
        var dataKeySize: Int
        val value = ByteArray(4)
        for (i in 0 until size) {
            dataKeySize = inp.readU()

            if (dataKeySize < 0) {
                continue
            }

            dataKey = ByteArray(
                dataKeySize
            )

            if (inp.read(
                dataKey,
                0,
                dataKeySize
            ) < 0) {
                continue
            }

            if (inp.read(
                value,
                0,
                4
            ) < 0) {
                continue
            }

            map[String(
                dataKey,
                0,
                dataKeySize,
                CHARSET_KEY
            )] = value.integer(0)
        }

        out.write(ANSWER_OK)
    }

}