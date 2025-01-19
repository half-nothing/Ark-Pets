package cn.harryh.arkpets.kt.database.model

import cn.harryh.arkpets.kt.database.entity.ModelVoiceClip
import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.int

object ModelVoiceClips : Table<ModelVoiceClip>("model_voice_clips") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val modelVoiceId = int("model_voice_id").references(ModelVoices) { it.modelVoice }
    val start = double("start").bindTo { it.start }
    val duration = double("duration").bindTo { it.duration }
}
