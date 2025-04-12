package good.damn.media.streaming

import android.media.MediaFormat
import good.damn.media.streaming.camera.models.MSMCameraId

data class MSMStream(
    val userId: Int,
    val host: String,
    val camera: MSMCameraId,
    val format: MediaFormat
)