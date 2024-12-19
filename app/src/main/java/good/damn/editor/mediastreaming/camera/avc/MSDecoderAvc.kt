package good.damn.editor.mediastreaming.camera.avc

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import good.damn.editor.mediastreaming.camera.avc.MSCameraAVC.Companion
import java.io.ByteArrayOutputStream

class MSDecoderAvc(
    width: Int,
    height: Int,
    rotation: Int,
    surfacePreview: Surface,
    private val stream: ByteArrayOutputStream
): MediaCodec.Callback() {

    companion object {
        private const val TAG = "MSEncoderAvc"
    }

    private var mFrame = ByteArray(0)

    // may throws Exception with no h264 codec
    private val mDecoder = MediaCodec.createDecoderByType(
        MSEncoderAvc.TYPE_AVC
    ).apply {

        val format = MediaFormat.createVideoFormat(
            MSEncoderAvc.TYPE_AVC,
            width,
            height
        ).apply {
            setInteger(
                MediaFormat.KEY_ROTATION,
                rotation
            )
        }

        configure(
            format,
            surfacePreview,
            null,
            0
        )

        setOutputSurface(
            surfacePreview
        )

        setCallback(
            this@MSDecoderAvc
        )

        start()
    }


    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {
        val inp = codec.getInputBuffer(
            index
        ) ?: return

        inp.clear()

        synchronized(
            stream
        ) {
            mFrame = stream.toByteArray()
            stream.reset()
        }

        inp.put(
            mFrame
        )

        codec.queueInputBuffer(
            index,
            0,
            mFrame.size,
            0,
            0
        )
    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        codec.getOutputBuffer(
            index
        )

        codec.releaseOutputBuffer(
            index,
            true
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