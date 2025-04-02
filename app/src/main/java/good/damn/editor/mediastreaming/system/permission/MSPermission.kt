package good.damn.editor.mediastreaming.system.permission

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MSPermission
: ActivityResultCallback<Map<String, Boolean>> {

    var onResultPermission: MSListenerOnResultPermission? = null

    private var mLauncher: ActivityResultLauncher<Array<String>>? = null

    fun register(
        activity: AppCompatActivity
    ) {
        mLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            this
        )
    }

    inline fun launch(
        permission: String
    ) = launch(
        arrayOf(
            permission
        )
    )

    fun launch(
        permissions: Array<String>
    ) {
        mLauncher?.launch(
            permissions
        )
    }

    override fun onActivityResult(
        result: Map<String, Boolean>
    ) {
        onResultPermission?.onResultPermissions(
            result
        )
    }
}