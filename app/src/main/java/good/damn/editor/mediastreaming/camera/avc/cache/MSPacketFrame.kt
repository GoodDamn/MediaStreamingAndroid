package good.damn.editor.mediastreaming.camera.avc.cache

import java.util.LinkedList

data class MSPacketFrame(
    val chunks: HashMap<Short, MSPacket>
)