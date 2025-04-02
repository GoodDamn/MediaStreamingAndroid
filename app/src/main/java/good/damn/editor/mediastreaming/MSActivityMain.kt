package good.damn.editor.mediastreaming

import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import good.damn.editor.mediastreaming.fragments.client.MSFragmentTestH264
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission
import good.damn.editor.mediastreaming.system.permission.MSPermission

class MSActivityMain
: AppCompatActivity(),
MSListenerOnResultPermission {

    val launcherPermission = MSPermission().apply {
        onResultPermission = this@MSActivityMain
    }

    private val mFragments = arrayOf(
        MSFragmentTestH264()
    )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val context = this

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy
                .Builder()
                .permitNetwork()
                .build()
        )

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

    override fun onResultPermissions(
        result: Map<String,Boolean>
    ) {
        mFragments.forEach {
            (it as? MSListenerOnResultPermission)
                ?.onResultPermissions(
                    result
                )
        }
    }

}