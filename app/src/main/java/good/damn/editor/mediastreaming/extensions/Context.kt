package good.damn.editor.mediastreaming.extensions

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.eap.EapSessionConfig.Builder
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat

inline fun Context.hasPermissionCamera() =
    hasPermission(
        Manifest.permission.CAMERA
    )

inline fun Context.hasPermissionMicrophone() =
    hasPermission(
        Manifest.permission.RECORD_AUDIO
    )

inline fun Context.hasPermission(
    permission: String
) = ContextCompat.checkSelfPermission(
    this,
    permission
) == PackageManager.PERMISSION_GRANTED

inline fun Context.supportsForegroundService() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

inline fun Context.toast(
    msg: String
) = Toast.makeText(
    this,
    msg,
    Toast.LENGTH_SHORT
).show()

inline fun Context.getNotificationManager() = getSystemService(
    Context.NOTIFICATION_SERVICE
) as? NotificationManager