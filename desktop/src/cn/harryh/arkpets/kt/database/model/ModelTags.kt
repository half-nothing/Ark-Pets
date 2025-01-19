package cn.harryh.arkpets.kt.database.model

import cn.harryh.arkpets.kt.database.entity.ModelTag
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object ModelTags : Table<ModelTag>("model_tags") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val modelId = int("model_id").references(ModelInfos) { it.modelInfo }
    val tag = varchar("tag").bindTo { it.tag }
}
