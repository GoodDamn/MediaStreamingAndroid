package good.damn.editor.mediastreaming.network.server

import good.damn.editor.mediastreaming.extensions.integer
import good.damn.editor.mediastreaming.extensions.short
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveData
import good.damn.editor.mediastreaming.network.server.listeners.MSListenerOnReceiveFramePiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MSReceiverCameraFramePiece
: MSListenerOnReceiveData {

    var onReceiveFramePiece: MSListenerOnReceiveFramePiece? = null

    override suspend fun onReceiveData(
        data: ByteArray
    ) {
        val fromIndex = data.integer(0)
        val toIndex = data.integer(4)

        onReceiveFramePiece?.onReceiveFramePiece(
            from = fromIndex,
            to = toIndex,
            8,
            data
        )
    }

}