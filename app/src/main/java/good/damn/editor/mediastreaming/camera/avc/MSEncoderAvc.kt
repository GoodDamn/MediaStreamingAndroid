package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.ByteArrayOutputStream

class MSEncoderAvc(
    width: Int,
    height: Int,
    rotation: Int
): MediaCodec.Callback() {

    companion object {
        private const val TAG = "MSEncoderAvc"
        const val TYPE_AVC = "video/avc"
    }

    // may throws Exception with no h264 codec
    private val mEncoder = MediaCodec.createEncoderByType(
        TYPE_AVC
    ).apply {

        val format = MediaFormat.createVideoFormat(
            TYPE_AVC,
            width,
            height
        ).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities
                    .COLOR_FormatSurface
            )

            setInteger(
                MediaFormat.KEY_BIT_RATE,
                2_000_000
            )

            setInteger(
                MediaFormat.KEY_FRAME_RATE,
                24
            )

            setInteger(
                MediaFormat.KEY_ROTATION,
                rotation
            )

            setInteger(
                MediaFormat.KEY_I_FRAME_INTERVAL,
                1
            )
        }

        configure(
            format,
            null,
            null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )

        setCallback(
            this@MSEncoderAvc
        )
    }

    val inputSurface = mEncoder.createInputSurface()
    val stream = ByteArrayOutputStream()

    fun start() {
        mEncoder.start()
    }

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        Log.d(TAG, "onInputBufferAvailable: ")
    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        val buffer = codec.getOutputBuffer(
            index
        ) ?: return

        Log.d(TAG, "onOutputBufferAvailable: $index ${info.size} ${buffer.capacity()}")

        val f = ByteArray(
            info.size
        )

        buffer.get(f)

        synchronized(
            stream
        ) {
            stream.write(f)
        }

        codec.releaseOutputBuffer(
            index,
            false
        )
    }

    override fun onError(
        codec: MediaCodec,
        e: MediaCodec.CodecException
    ) {
        Log.d(TAG, "onError: ${e.localizedMessage}")
    }

    override fun onOutputFormatChanged(
        codec: MediaCodec,
        format: MediaFormat
    ) {
        Log.d(TAG, "onOutputFormatChanged: $format")
    }

}