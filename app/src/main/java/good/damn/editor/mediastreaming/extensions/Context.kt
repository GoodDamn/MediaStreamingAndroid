package good.damn.editor.mediastreaming.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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


inline fun Context.toast(
    msg: String
) = Toast.makeText(
    this,
    msg,
    Toast.LENGTH_SHORT
).show()