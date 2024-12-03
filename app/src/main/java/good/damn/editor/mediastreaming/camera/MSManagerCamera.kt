package good.damn.editor.mediastreaming.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import good.damn.editor.mediastreaming.camera.listeners.MSListenerOnOpenCamera
import java.util.LinkedList

@SuppressLint("MissingPermission")
class MSManagerCamera(
    context: Context
) {

    private val manager = context.getSystemService(
        Context.CAMERA_SERVICE
    ) as CameraManager

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