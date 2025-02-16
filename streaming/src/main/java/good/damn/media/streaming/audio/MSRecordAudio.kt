package good.damn.media.streaming.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MSRecordAudio
: AudioRecord(
    AudioSource.MIC,
    good.damn.media.streaming.audio.MSRecordAudio.Companion.DEFAULT_SAMPLE_RATE,
    good.damn.media.streaming.audio.MSRecordAudio.Companion.DEFAULT_CHANNEL,
    good.damn.media.streaming.audio.MSRecordAudio.Companion.DEFAULT_ENCODING,
    good.damn.media.streaming.audio.MSRecordAudio.Companion.DEFAULT_BUFFER_SIZE
) {

    companion object {
        private val TAG = good.damn.media.streaming.audio.MSRecordAudio::class.simpleName
        private const val DEFAULT_BUFFER_SIZE = 256

        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    init {
        Log.d(good.damn.media.streaming.audio.MSRecordAudio.Companion.TAG, "init:")
    }

    var onSampleListener: good.damn.media.streaming.audio.MSListenerOnSamplesRecord? = null

    private var mIsPaused = false

    private val mSampleData = ByteArray(
        good.damn.media.streaming.audio.MSRecordAudio.Companion.DEFAULT_BUFFER_SIZE
    )

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

    private inline fun runStream() {
        val sample = read(
            mSampleData,
            0,
            mSampleData.size
        )

        when (sample) {
            ERROR_INVALID_OPERATION -> {
                Log.d(good.damn.media.streaming.audio.MSRecordAudio.Companion.TAG, "runStream: INVALID_OPERATION")
            }

            ERROR_BAD_VALUE -> {
                Log.d(good.damn.media.streaming.audio.MSRecordAudio.Companion.TAG, "runStream: BAD_VALUE")
            }

            ERROR_DEAD_OBJECT -> {
                Log.d(good.damn.media.streaming.audio.MSRecordAudio.Companion.TAG, "runStream: DEAD_OBJECT")
            }

            ERROR -> {
                Log.d(good.damn.media.streaming.audio.MSRecordAudio.Companion.TAG, "runStream: ERROR")
            }

            else -> {
                onSampleListener?.onRecordSamples(
                    mSampleData,
                    0,
                    sample
                )
            }
        }

    }
}