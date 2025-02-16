package good.damn.media.streaming.audio

interface MSListenerOnSamplesRecord {
    fun onRecordSamples(
        samples: ByteArray,
        position: Int,
        len: Int
    )
}