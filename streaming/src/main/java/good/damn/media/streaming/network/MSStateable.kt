package good.damn.media.streaming.network

import kotlinx.coroutines.Job

interface MSStateable {
    fun start()
    fun stop()
    fun release()
}