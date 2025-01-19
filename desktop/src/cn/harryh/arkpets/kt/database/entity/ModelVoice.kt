package cn.harryh.arkpets.kt.database.entity

import org.ktorm.entity.Entity

interface ModelVoice : Entity<ModelVoice> {
    var id: Int
    var modelInfo: ModelInfo
    var language: String
    var duration: Double
    var exist: Boolean

    companion object : Entity.Factory<ModelVoice>()
}
