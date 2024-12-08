package good.damn.editor.mediastreaming.camera.models

data class MSCameraModelID(
    val logical: String,
    val physical: String? = null
) {
    override fun toString() = "$logical:$physical"
}