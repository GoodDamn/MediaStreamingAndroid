package good.damn.editor.mediastreaming.system.permission

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MSPermission
: ActivityResultCallback<Boolean> {

    var onResultPermission: MSListenerOnResultPermission? = null

    private var mLauncher: ActivityResultLauncher<String>? = null

    private var mRequestedPermission: String? = null

    fun register(
        activity: AppCompatActivity
    ) {
        mLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            this
        )
    }

    fun launch(
        permission: String
    ) {
        mRequestedPermission = permission
        mLauncher?.launch(
            permission
        )
    }

    override fun onActivityResult(
        result: Boolean
    ) {
        val perm = mRequestedPermission
            ?: return

        onResultPermission?.onResultPermission(
            perm,
            result
        )
    }
}