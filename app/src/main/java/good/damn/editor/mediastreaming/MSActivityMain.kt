package good.damn.editor.mediastreaming

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import good.damn.editor.mediastreaming.fragments.client.MSFragmentClient
import good.damn.editor.mediastreaming.fragments.MSFragmentServer
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission

class MSActivityMain
: AppCompatActivity(),
MSListenerOnResultPermission {

    val launcherPermission = MSPermission().apply {
        onResultPermission = this@MSActivityMain
    }

    private val mFragments = arrayOf(
        MSFragmentClient(),
        MSFragmentServer()
    )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = this

        ViewPager2(
            context
        ).apply {
            adapter = object: FragmentStateAdapter(
                supportFragmentManager,
                lifecycle
            ) {
                override fun getItemCount() = mFragments.size

                override fun createFragment(
                    position: Int
                ) = mFragments[position]

            }

            setContentView(
                this
            )
        }

        launcherPermission.register(
            this@MSActivityMain
        )

    }

    override fun onResultPermission(
        permission: String,
        result: Boolean
    ) {
        mFragments.forEach {
            (it as? MSListenerOnResultPermission)
                ?.onResultPermission(
                    permission,
                    result
                )
        }
    }

}