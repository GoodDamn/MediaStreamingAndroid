package good.damn.editor.mediastreaming.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MSAudioRecord
: AudioRecord(
    AudioSource.MIC,
    DEFAULT_SAMPLE_RATE,
    DEFAULT_CHANNEL,
    DEFAULT_ENCODING,
    DEFAULT_BUFFER_SIZE
) {

    companion object {
        private val TAG = MSAudioRecord::class.simpleName
        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_BUFFER_SIZE = 32
        const val DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_STEREO
        const val DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    init {
        Log.d(TAG, "init:")
    }

    var onSampleListener: MSListenerOnSamplesRecord? = null

    private var mIsPaused = false

    private val mSampleData = ByteArray(DEFAULT_BUFFER_SIZE)

    private val mScope = CoroutineScope(
        Dispatchers.IO
    )

    override fun release() {
        super.release()
        mIsPaused = true
    }

    override fun stop() {
        super.stop()
        mIsPaused = true
    }

    override fun startRecording() {
        super.startRecording()
        mIsPaused = false
        mScope.launch {
            while (!mIsPaused) {
                runStream()
            }
        }
    }

    private suspend inline fun runStream() {
        val sample = read(
            mSampleData,
            0,
            mSampleData.size
        )

        when (sample) {
            ERROR_INVALID_OPERATION -> {
                Log.d(TAG, "runStream: INVALID_OPERATION")
            }

            ERROR_BAD_VALUE -> {
                Log.d(TAG, "runStream: BAD_VALUE")
            }

            ERROR_DEAD_OBJECT -> {
                Log.d(TAG, "runStream: DEAD_OBJECT")
            }

            ERROR -> {
                Log.d(TAG, "runStream: ERROR")
            }

            else -> {
                Log.d(TAG, "runStream: $sample")
                onSampleListener?.onRecordSamples(
                    mSampleData,
                    0,
                    sample
                )
            }
        }

    }
}