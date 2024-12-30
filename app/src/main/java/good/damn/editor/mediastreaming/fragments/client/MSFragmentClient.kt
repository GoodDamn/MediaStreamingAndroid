package good.damn.editor.mediastreaming.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import good.damn.editor.mediastreaming.system.permission.MSListenerOnResultPermission

class MSFragmentClient
: Fragment(),
MSListenerOnResultPermission {

    val fragmentRoomList = MSFragmentClientRoomList().apply {
        rootFragment = this@MSFragmentClient
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = context?.run {
        return@run FrameLayout(
            this
        ).apply {
            id = ViewCompat.generateViewId()
        }
    }

    override fun onStart() {
        super.onStart()
        setFragment(
            fragmentRoomList
        )
    }

    fun setFragment(
        fragment: Fragment
    ) = view?.run {
        childFragmentManager
            .beginTransaction()
            .replace(
                id,
                fragment
            ).commit()
    }

    override fun onResultPermission(
        permission: String,
        result: Boolean
    ) {

    }

}