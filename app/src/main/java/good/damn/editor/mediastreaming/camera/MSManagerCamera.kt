package good.damn.editor.mediastreaming.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.util.Size
import java.util.LinkedList

@SuppressLint("MissingPermission")
class MSManagerCamera(
    context: Context
) {

    private val manager = context.getSystemService(
        Context.CAMERA_SERVICE
    ) as CameraManager

    val cameraIds = manager.cameraIdList

    fun getCameraId(
        characteristic: CameraCharacteristics.Key<Int>,
        metadata: Int
    ): List<String> {
        val cameras = LinkedList<String>()
        manager.cameraIdList.forEach {
            manager.getCameraCharacteristics(
                it
            ).apply {
                if (get(characteristic) == metadata) {
                    cameras.add(it)
                }
            }
        }

        return cameras
    }

    fun getOutputSizes(
        cameraId: String,
        format: Int
    ): Array<Size>? {
        manager.getCameraCharacteristics(
            cameraId
        ).apply {
            val streamMap = get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )

            return streamMap?.getOutputSizes(
                format
            )
        }


    }

    fun getRotationInitial(
        cameraId: String
    ) = manager.getCameraCharacteristics(
        cameraId
    ).get(
        CameraCharacteristics.SENSOR_ORIENTATION
    )

    fun openCamera(
        cameraId: String,
        listener: CameraDevice.StateCallback
    ) {
        manager.openCamera(
            cameraId,
            listener,
            Handler(
                Looper.getMainLooper()
            )
        )
    }
}