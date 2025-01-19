package cn.harryh.arkpets.kt.extension

import cn.harryh.arkpets.kt.database.model.MetadataList
import cn.harryh.arkpets.kt.database.model.ModelAssets
import cn.harryh.arkpets.kt.database.model.ModelInfos
import cn.harryh.arkpets.kt.database.model.ModelTags
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf

val Database.metadata get() = this.sequenceOf(MetadataList)
val Database.modelAssets get() = this.sequenceOf(ModelAssets)
val Database.modelInfos get() = this.sequenceOf(ModelInfos)
val Database.modelTags get() = this.sequenceOf(ModelTags)
