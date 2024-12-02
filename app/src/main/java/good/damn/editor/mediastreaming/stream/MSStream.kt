package good.damn.editor.mediastreaming.stream

interface MSStream {
    fun start()

    fun stop()

    fun release()
}