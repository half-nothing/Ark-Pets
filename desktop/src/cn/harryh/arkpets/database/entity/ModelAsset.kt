package cn.harryh.arkpets.database.entity

import org.ktorm.entity.Entity

interface ModelAsset : Entity<ModelAsset> {
    var id: Int
    var modelInfo: ModelInfo
    var type: String
    var filename: String

    companion object : Entity.Factory<ModelAsset>()
}
