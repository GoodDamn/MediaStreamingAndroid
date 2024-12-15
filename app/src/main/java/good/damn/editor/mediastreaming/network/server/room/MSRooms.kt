package good.damn.editor.mediastreaming.network.server.room

import java.lang.reflect.TypeVariable
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class MSRooms {

    companion object {
        const val SIZE_ROOM_ID = 1
    }

    private val mRooms = ConcurrentHashMap<
        Int, MSRoom
    >(32)

    val size: Int
        get() = mRooms.size

    val entries: Set<Map.Entry<Int, MSRoom>>
        get() = mRooms.entries

    fun getRoomById(
        id: Int
    ) = mRooms[id]

    fun addRoom(
        room: MSRoom
    ) = mRooms.put(
        generateRoomId(),
        room
    )

    fun removeRoom(
        id: Int
    ) = mRooms.remove(
        id
    )

    private inline fun generateRoomId() = Random.nextInt(255)

}