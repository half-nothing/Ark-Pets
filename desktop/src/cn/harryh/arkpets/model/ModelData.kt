package cn.harryh.arkpets.model

data class ModelData(
    val storageDirectory: Map<String, String>,
    val sortTags: Map<String, String>,
    val gameDataVersionDescription: String,
    val gameDataServerRegion: String,
    val data: Map<String, ModelConfig>,
    val arkPetsCompatibility: List<Int>
)
