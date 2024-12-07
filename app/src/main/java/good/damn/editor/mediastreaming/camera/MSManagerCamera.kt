package good.damn.editor.mediastreaming.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import good.damn.editor.mediastreaming.camera.models.MSCameraModelID
import java.util.LinkedList
import kotlin.math.log

@SuppressLint("MissingPermission")
class MSManagerCamera(
    context: Context
) {

    private val manager = context.getSystemService(
        Context.CAMERA_SERVICE
    ) as CameraManager

    fun getCameraIds(): List<MSCameraModelID> {
        val list = LinkedList<MSCameraModelID>()

        for (logicalId in manager.cameraIdList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getCharacteristics(
                    logicalId
                ).physicalCameraIds.apply {
                    if (isEmpty()) {
                        list.add(
                            MSCameraModelID(
                                logicalId
                            )
                        )
                        return@apply
                    }

                    forEach {
                        list.add(
                            MSCameraModelID(
                                logicalId,
                                it
                            )
                        )
                    }
                }

                continue
            }

            list.add(
                MSCameraModelID(
                    logicalId
                )
            )
        }

        return list
    }

    fun getCharacteristics(
        cameraId: String
    ) = manager.getCameraCharacteristics(
        cameraId
    )

    fun openCamera(
        cameraId: String,
        listener: CameraDevice.StateCallback,
        handler: Handler
    ) {
        manager.openCamera(
            cameraId,
            listener,
            handler
        )
    }
}