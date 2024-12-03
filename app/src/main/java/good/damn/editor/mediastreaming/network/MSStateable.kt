package good.damn.editor.mediastreaming.network

import kotlinx.coroutines.Job

interface MSStateable {
    fun start(): Job
    fun stop()
    fun release()
}