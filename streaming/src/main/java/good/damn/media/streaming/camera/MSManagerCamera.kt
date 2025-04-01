package good.damn.media.streaming.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import good.damn.media.streaming.camera.models.MSCameraModelID
import java.util.LinkedList
import kotlin.math.log

@SuppressLint("MissingPermission")
class MSManagerCamera(
    context: Context?
) {

    private val manager = context?.getSystemService(
        Context.CAMERA_SERVICE
    ) as CameraManager

    fun getCameraIds(): List<MSCameraModelID> {
        val list = LinkedList<MSCameraModelID>()

        for (logicalId in manager.cameraIdList) {
            val character = getCharacteristics(
                logicalId
            )

            if (character.get(
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
            ) == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                list.add(
                    MSCameraModelID(
                        logicalId,
                        isLegacy = true,
                        characteristics = character
                    )
                )
                continue
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                character.physicalCameraIds.apply {
                    if (isEmpty()) {
                        list.add(
                            MSCameraModelID(
                                logicalId,
                                characteristics = character
                            )
                        )
                        return@apply
                    }

                    forEach {
                        list.add(
                            MSCameraModelID(
                                logicalId,
                                it,
                                characteristics = character
                            )
                        )
                    }
                }

                continue
            }

            list.add(
                MSCameraModelID(
                    logicalId,
                    characteristics = character
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
        cameraId: MSCameraModelID,
        listener: CameraDevice.StateCallback,
        handler: Handler
    ) {
        manager.openCamera(
            cameraId.logical,
            listener,
            handler
        )
    }
}