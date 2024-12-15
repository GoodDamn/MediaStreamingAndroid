package good.damn.editor.mediastreaming.network.server.room

import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedDeque

data class MSRoom(
    val users: ConcurrentLinkedDeque<
        MSRoomUser
    > = ConcurrentLinkedDeque()
) {
    override fun toString() = hashCode()
        .toString()
}