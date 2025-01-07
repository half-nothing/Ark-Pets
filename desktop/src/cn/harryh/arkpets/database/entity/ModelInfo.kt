package cn.harryh.arkpets.database.entity

import org.ktorm.entity.Entity

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

    companion object : Entity.Factory<ModelInfo>()
}
