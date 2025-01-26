package cn.harryh.arkpets.kt.repository

data class RepositoryConfig(
    val repositoryName: String,
    val localPath: String,
    val remotePath: String,
    val metadataFileName: String,
    val metadataFilePath: String
)
