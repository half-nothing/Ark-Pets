package cn.harryh.arkpets.kt.database.model

import cn.harryh.arkpets.kt.database.entity.ModelVoice
import org.ktorm.schema.*

object ModelVoices : Table<ModelVoice>("model_voice") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val modelId = int("model_id").references(ModelInfos) { it.modelInfo }
    val language = varchar("language").bindTo { it.language }
    val duration = double("duration").bindTo { it.duration }
    val exist = boolean("exist").bindTo { it.exist }
}
