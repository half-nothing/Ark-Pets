package cn.harryh.arkpets.model

data class ModelConfig(
    val assetId: String,
    val type: String,
    val style: String?,
    val name: String,
    val appellation: String?,
    val skinGroupId: String,
    val skinGroupName: String,
    val sortTags: List<String>,
    val assetList: Map<String, Any>
)
