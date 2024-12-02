package good.damn.editor.mediastreaming

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MSActivityMain
: AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = this

        LinearLayout(
            context
        ).apply {

            orientation = LinearLayout.VERTICAL

            Button(
                context
            ).apply {

                setOnClickListener {
                    onClickBtnClient(this)
                }

                text = "Client"

                addView(
                    this,
                    -1,
                    -2
                )
            }

            Button(
                context
            ).apply {

                setOnClickListener {
                    onClickBtnServer(this)
                }

                text = "Server"

                addView(
                    this,
                    -1,
                    -2
                )
            }

            setContentView(
                this
            )
        }
    }

    private inline fun onClickBtnClient(
        btn: Button
    ) {
        startActivity(
            Intent(
                btn.context,
                MSActivityClient::class.java
            )
        )
    }

    private inline fun onClickBtnServer(
        btn: Button
    ) {
        startActivity(
            Intent(
                btn.context,
                MSActivityServer::class.java
            )
        )
    }

}