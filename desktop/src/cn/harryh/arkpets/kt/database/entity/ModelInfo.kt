package cn.harryh.arkpets.kt.database.entity

import org.ktorm.entity.Entity

@Suppress("ComplexInterface")
interface ModelInfo : Entity<ModelInfo> {
    var id: Int
    var assetId: String
    var storePath: String
    var type: String
    var style: String?
    var name: String
    var appellation: String?
    var skinGroupId: String
    var skinGroupName: String
    var md5: String
    var exist: Int

    companion object : Entity.Factory<ModelInfo>()
}
