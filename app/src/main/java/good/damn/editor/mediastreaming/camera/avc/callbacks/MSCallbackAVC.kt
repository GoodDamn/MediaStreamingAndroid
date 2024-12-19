package good.damn.editor.mediastreaming.camera.avc.callbacks

import android.media.MediaCodec
import android.media.MediaFormat

class MSCallbackAVC
: MediaCodec.Callback() {

    override fun onInputBufferAvailable(
        codec: MediaCodec,
        index: Int
    ) {

    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {

    }

    override fun onError(
        codec: MediaCodec,
        e: MediaCodec.CodecException
    ) {

    }

    override fun onOutputFormatChanged(
        codec: MediaCodec,
        format: MediaFormat
    ) {

    }

    override fun onCryptoError(
        codec: MediaCodec,
        e: MediaCodec.CryptoException
    ) {

    }
}