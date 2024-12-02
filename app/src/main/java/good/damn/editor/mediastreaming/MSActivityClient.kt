package good.damn.editor.mediastreaming

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import good.damn.editor.mediastreaming.audio.stream.MSStreamAudioInput
import java.net.InetAddress

class MSActivityClient
: AppCompatActivity() {

    companion object {
        private val TAG = MSActivityClient::class.simpleName
    }

    private var mStreamAudio: MSStreamAudioInput? = null

    private var mLauncherPermission: ActivityResultLauncher<String>? = null

    private var mEditText: EditText? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = this

        mEditText = EditText(
            context
        ).apply {
            hint = "Host"
        }

        LinearLayout(
            context
        ).apply {

            orientation = LinearLayout
                .VERTICAL

            addView(
                mEditText,
                -1,
                -2
            )

            Button(
                context
            ).apply {
                text = "Call"

                setOnClickListener {
                    onClickBtnCall(this)
                }

                addView(
                    this,
                    -1,
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
                    -1,
                    -2
                )
            }

            layoutParams = FrameLayout.LayoutParams(
                -1,
                -1
            )

            setContentView(
                this
            )
        }

        mLauncherPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                mStreamAudio = MSStreamAudioInput()
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
        Log.d(TAG, "onClickBtnCall: $mStreamAudio")
        if (mStreamAudio == null) {
            mLauncherPermission?.launch(
                Manifest.permission.RECORD_AUDIO
            )
            return
        }

        mStreamAudio?.apply {
            host = InetAddress.getByName(
                mEditText?.text?.toString()
            )
            start()
        }
    }

    private inline fun onClickBtnDecline(
        btn: Button
    ) {
        mStreamAudio?.stop()
    }

}