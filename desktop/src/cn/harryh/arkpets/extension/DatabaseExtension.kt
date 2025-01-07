package cn.harryh.arkpets.extension

import cn.harryh.arkpets.database.entity.Metadata
import cn.harryh.arkpets.database.entity.ModelAsset
import cn.harryh.arkpets.database.entity.ModelInfo
import cn.harryh.arkpets.database.entity.ModelTag
import cn.harryh.arkpets.database.model.MetadataList
import cn.harryh.arkpets.database.model.ModelAssets
import cn.harryh.arkpets.database.model.ModelInfos
import cn.harryh.arkpets.database.model.ModelTags
import org.ktorm.database.Database
import org.ktorm.entity.add
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update

val Database.metadata get() = this.sequenceOf(MetadataList)
val Database.modelAssets get() = this.sequenceOf(ModelAssets)
val Database.modelInfos get() = this.sequenceOf(ModelInfos)
val Database.modelTags get() = this.sequenceOf(ModelTags)

fun Database.addMetaData(metadata: Metadata) {
    this.metadata.add(metadata)
}

fun Database.updateMetaData(metadata: Metadata) {
    this.metadata.update(metadata)
}

fun Database.addModelInfo(modelInfo: ModelInfo) {
    modelInfos.add(modelInfo)
}

fun Database.updateModelInfo(modelInfo: ModelInfo) {
    modelInfos.update(modelInfo)
}

fun Database.addModelTags(modelTag: ModelTag) {
    modelTags.add(modelTag)
}

fun Database.updateModelTags(modelTag: ModelTag) {
    modelTags.update(modelTag)
}

fun Database.addModelAssets(modelAsset: ModelAsset) {
    modelAssets.add(modelAsset)
}

fun Database.updateModelAssets(modelAsset: ModelAsset) {
    modelAssets.update(modelAsset)
}
