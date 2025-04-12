package good.damn.editor.mediastreaming.views

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import good.damn.editor.mediastreaming.MSApp
import good.damn.editor.mediastreaming.clicks.MSClickOnSelectCamera
import good.damn.editor.mediastreaming.clicks.MSListenerOnSelectCamera
import good.damn.editor.mediastreaming.views.dialogs.option.MSDialogOptionsH264
import good.damn.media.streaming.MSTypeDecoderSettings
import good.damn.media.streaming.camera.MSManagerCamera

class MSViewFragmentTestH264(
    context: Context,
    onSelectCamera: MSListenerOnSelectCamera,
    private val onClickOptions: OnClickListener? = null
): LinearLayout(
    context
) {

    var editTextHost: AppCompatEditText? = null
        private set

    var layoutSurfaces: LinearLayout? = null
        private set

    init {
        orientation = VERTICAL

        AppCompatEditText(
            context
        ).apply {
            editTextHost = this
            setText(
                "127.0.0.1"
            )

            addView(
                this,
                -1,
                -2
            )
        }

        FrameLayout(
            context
        ).let {
            setBackgroundColor(0)

            ScrollView(
                context
            ).let { scrollView ->
                setBackgroundColor(0)
                LinearLayout(
                    context
                ).apply {
                    layoutSurfaces = this
                    orientation = VERTICAL
                    setBackgroundColor(0)

                    layoutParams = FrameLayout.LayoutParams(
                        -2,
                        -2
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    scrollView.addView(
                        this
                    )
                }

                it.addView(
                    scrollView
                )
            }

            LinearLayout(
                context
            ).apply {

                orientation = VERTICAL

                MSManagerCamera(
                    context
                ).getCameraIds().forEach {
                    addView(
                        Button(
                            context
                        ).apply {
                            text = "${it.logical}_${it.physical ?: ""}"
                            setOnClickListener(
                                MSClickOnSelectCamera(
                                    it
                                ).apply {
                                    this.onSelectCamera = onSelectCamera
                                }
                            )
                        },
                        (0.15f * MSApp.width).toInt(),
                        -2
                    )
                }

                onClickOptions?.let {
                    Button(
                        context
                    ).apply {
                        text = "options"

                        setOnClickListener(it)

                        addView(
                            this,
                            -2,
                            -2
                        )
                    }
                }


                it.addView(
                    this,
                    -1,
                    -2
                )
            }


            addView(
                it
            )
        }
    }

}