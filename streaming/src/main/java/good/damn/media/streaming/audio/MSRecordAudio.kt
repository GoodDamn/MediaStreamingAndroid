package good.damn.media.streaming.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MSRecordAudio
: Runnable {

    companion object {
        private val TAG = MSRecordAudio::class.simpleName
        private const val DEFAULT_BUFFER_SIZE = 256

        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    var onSampleListener: MSListenerOnSamplesRecord? = null
    var isRunning = false
        private set

    private var mHandler: Handler? = null

    private val mRecorder = AudioRecord(
        AudioSource.MIC,
        DEFAULT_SAMPLE_RATE,
        DEFAULT_CHANNEL,
        DEFAULT_ENCODING,
        DEFAULT_BUFFER_SIZE
    )

    private val mSampleData = ByteArray(
        DEFAULT_BUFFER_SIZE
    )

    fun release() {
        mRecorder.release()
        isRunning = false
    }

    fun stop() {
        mRecorder.stop()
        isRunning = false
    }

    fun start(
        handler: Handler
    ) {
        mHandler = handler
        mRecorder.startRecording()
        isRunning = true
        handler.post(this)
    }

    override fun run() {
        if (!isRunning) {
            return
        }

        val sample = mRecorder.read(
            mSampleData,
            0,
            mSampleData.size
        )

        when (sample) {
            AudioRecord.ERROR_INVALID_OPERATION -> {
                Log.d(TAG, "runStream: INVALID_OPERATION")
            }

            AudioRecord.ERROR_BAD_VALUE -> {
                Log.d(TAG, "runStream: BAD_VALUE")
            }

            AudioRecord.ERROR_DEAD_OBJECT -> {
                Log.d(TAG, "runStream: DEAD_OBJECT")
            }

            AudioRecord.ERROR -> {
                Log.d(TAG, "runStream: ERROR")
            }

            else -> onSampleListener?.onRecordSamples(
                mSampleData,
                0,
                sample
            )
        }
    }
}