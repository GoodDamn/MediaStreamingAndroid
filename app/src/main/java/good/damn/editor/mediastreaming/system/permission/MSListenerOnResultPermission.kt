package good.damn.editor.mediastreaming.system.permission

interface MSListenerOnResultPermission {
    fun onResultPermission(
        permission: String,
        result: Boolean
    )
}