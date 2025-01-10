package cn.harryh.arkpets.kt.database.model

import cn.harryh.arkpets.kt.database.entity.ModelAsset
import org.ktorm.dsl.isNotNull
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object ModelAssets : Table<ModelAsset>("model_assets") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val modelId = int("model_id").references(ModelInfos) { it.modelInfo }.isNotNull()
    val type = varchar("type").bindTo { it.type }.isNotNull()
    val filename = varchar("filename").bindTo { it.filename }.isNotNull()
}
