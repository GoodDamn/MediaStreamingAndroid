package good.damn.editor.mediastreaming.network.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface MSServer {
    fun start(): Job
    fun stop()
    fun release()
}