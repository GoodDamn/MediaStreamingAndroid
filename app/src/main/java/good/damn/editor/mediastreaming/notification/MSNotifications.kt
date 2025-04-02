package good.damn.editor.mediastreaming.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import good.damn.editor.mediastreaming.extensions.getNotificationManager

class MSNotifications {
    companion object {

        private const val CHANNEL_ID_FOREGROUND_STREAM = "foregroundStream"

        @RequiresApi(api = Build.VERSION_CODES.O)
        fun foregroundStream(
            title: String,
            text: String,
            context: Context
        ): Notification? {
            val channel = NotificationChannel(
                CHANNEL_ID_FOREGROUND_STREAM,
                "Foreground streaming",
                NotificationManager.IMPORTANCE_LOW
            )

            context.getNotificationManager()
                ?.createNotificationChannel(channel)
                ?: return null

            return NotificationCompat.Builder(
                context,
                CHANNEL_ID_FOREGROUND_STREAM
            ).setSound(
                null
            ).setContentTitle(
                title
            ).setContentText(
                text
            ).build()
        }
    }
}