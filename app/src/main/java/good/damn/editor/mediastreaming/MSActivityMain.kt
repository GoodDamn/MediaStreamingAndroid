package good.damn.editor.mediastreaming

import android.Manifest
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.MSAudioRecord
import good.damn.editor.mediastreaming.audio.MSListenerOnSamplesRecord
import good.damn.editor.mediastreaming.audio.stream.MSStreamAudio
import good.damn.editor.mediastreaming.extensions.hasPermissionMicrophone

class MSActivityMain
: AppCompatActivity() {

    private var mStreamAudio: MSStreamAudio? = null

    private var mLauncherPermission: ActivityResultLauncher<String>? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = this

        FrameLayout(
            context
        ).let { root ->

            LinearLayout(
                context
            ).apply {

                orientation = LinearLayout
                    .VERTICAL

                Button(
                    context
                ).apply {
                    text = "Call"

                    setOnClickListener {
                        onClickBtnCall(this)
                    }

                    addView(
                        this,
                        -2,
                        -2
                    )
                }

                Button(
                    context
                ).apply {
                    text = "Decline"

                    setOnClickListener {
                        onClickBtnDecline(this)
                    }

                    addView(
                        this,
                        -2,
                        -2
                    )
                }

                layoutParams = FrameLayout.LayoutParams(
                    -2,
                    -2
                ).apply {
                    gravity = Gravity.CENTER
                }

                root.addView(
                    this
                )
            }

            setContentView(
                root
            )
        }

        mLauncherPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                mStreamAudio = MSStreamAudio()
            }
        }.apply {
            launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    override fun onStop() {
        super.onStop()
        mStreamAudio?.release()
    }

    private inline fun onClickBtnCall(
        btn: Button
    ) {
        if (mStreamAudio == null) {
            mLauncherPermission?.launch(
                Manifest.permission.RECORD_AUDIO
            )
            return
        }
        mStreamAudio?.start()
    }

    private inline fun onClickBtnDecline(
        btn: Button
    ) {
        mStreamAudio?.stop()
    }

}