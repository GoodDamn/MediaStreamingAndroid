package good.damn.editor.mediastreaming.network.server.accepters

import good.damn.editor.mediastreaming.extensions.readU
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnAcceptClient
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.room.MSRoomUser
import good.damn.editor.mediastreaming.network.server.room.MSRooms
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import kotlin.random.Random

class MSAccepterConnectRoom(
    private val mRooms: MSRooms
): MSListenerOnAcceptClient {

    override fun onAcceptClient(
        fromAddress: InetAddress,
        inp: InputStream,
        out: OutputStream
    ) {
        if (inp.read() != 0xA) {
            inp.close()
            out.close()
            return
        }

        mRooms.getRoomById(
            inp.readU()
        )?.apply {
            val newUserId = Random.nextInt(255)

            out.write(
                newUserId
            )

            out.write(
                users.size
            )

            users.forEach {
                out.write(
                    it.id
                )
            }

            users.add(
                MSRoomUser(
                    newUserId,
                    fromAddress,
                    7777
                )
            )

        }

    }
}