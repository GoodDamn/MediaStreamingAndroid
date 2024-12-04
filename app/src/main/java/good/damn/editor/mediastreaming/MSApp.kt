package good.damn.editor.mediastreaming

import android.app.Application
import android.os.Handler
import android.os.Looper

class MSApp: Application() {

    companion object {
        val handler = Handler(
            Looper.getMainLooper()
        )

        inline fun ui(
            run: Runnable
        ) = handler.post(run)
    }
}