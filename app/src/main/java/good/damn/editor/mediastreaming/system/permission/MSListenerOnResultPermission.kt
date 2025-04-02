package good.damn.editor.mediastreaming.system.permission

interface MSListenerOnResultPermission {
    fun onResultPermissions(
        result: Map<String,Boolean>
    )
}