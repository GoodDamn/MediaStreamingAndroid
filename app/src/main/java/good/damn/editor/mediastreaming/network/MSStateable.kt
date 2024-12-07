package good.damn.editor.mediastreaming.network

import kotlinx.coroutines.Job

interface MSStateable {
    fun start()
    fun stop()
    fun release()
}