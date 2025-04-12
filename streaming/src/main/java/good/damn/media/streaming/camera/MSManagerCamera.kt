package good.damn.media.streaming.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.os.Handler
import good.damn.media.streaming.camera.models.MSMCameraId
import java.util.LinkedList

@SuppressLint("MissingPermission")
class MSManagerCamera(
    context: Context?
) {

    private val manager = context?.getSystemService(
        Context.CAMERA_SERVICE
    ) as CameraManager

    fun getCameraIds(): List<MSMCameraId> {
        val list = LinkedList<MSMCameraId>()

        for (logicalId in manager.cameraIdList) {
            val character = getCharacteristics(
                logicalId
            )

            if (character.get(
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
            ) == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                list.add(
                    MSMCameraId(
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
                            MSMCameraId(
                                logicalId,
                                characteristics = character
                            )
                        )
                        return@apply
                    }

                    forEach {
                        list.add(
                            MSMCameraId(
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
                MSMCameraId(
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
        cameraId: MSMCameraId,
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