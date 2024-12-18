package good.damn.editor.mediastreaming

import android.app.Application
import android.os.Handler
import android.os.Looper

class MSApp: Application() {

    companion object {
        val handler = Handler(
            Looper.getMainLooper()
        )

        var dp = 0f
        var width = 0
        var height = 0

        inline fun ui(
            run: Runnable
        ) = handler.post(run)
    }

    override fun onCreate() {
        super.onCreate()

        resources.displayMetrics.apply {
            dp = density
            width = widthPixels
            height = heightPixels
        }
    }
}