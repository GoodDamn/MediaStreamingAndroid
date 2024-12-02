package good.damn.editor.mediastreaming.audio

interface MSListenerOnSamplesRecord {
    fun onRecordSamples(
        samples: ByteArray,
        position: Int,
        len: Int
    )
}