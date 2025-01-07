package cn.harryh.arkpets.utils

import cn.harryh.arkpets.Const
import cn.harryh.arkpets.database.DatabaseHelper
import cn.harryh.arkpets.database.entity.Metadata
import cn.harryh.arkpets.database.entity.ModelAsset
import cn.harryh.arkpets.database.entity.ModelInfo
import cn.harryh.arkpets.database.entity.ModelTag
import cn.harryh.arkpets.extension.*
import cn.harryh.arkpets.model.ModelConfig
import cn.harryh.arkpets.model.ModelData
import com.alibaba.fastjson2.JSON
import org.ktorm.entity.toMutableList
import java.io.File

object ModelManager {
    private val database by lazy {
        DatabaseHelper.getDatabase()
    }

    private val metadataList by lazy {
        database.metadata.toMutableList()
    }

    private val modelInfoList by lazy {
        database.modelInfos.toMutableList()
    }

    private lateinit var storageDirectory: Map<String, String>

    private fun handleMetadata(group: String, key: String, value: String) {
        metadataList.getOrNull(key)?.let {
            if (it.value != value) {
                it.value = value
                database.updateMetaData(it)
            }
            metadataList.remove(it)
        } ?: run {
            database.addMetaData(
                Metadata {
                    this.group = group
                    this.key = key
                    this.value = value
                }
            )
        }
    }

    private fun checkMetadata(data: ModelData) {
        data.sortTags.forEach { handleMetadata(Const.MetadataGroup.sortTagGroup, it.key, it.value) }
        handleMetadata(
            Const.MetadataGroup.defaultGroup,
            ModelData::gameDataVersionDescription.name,
            data.gameDataVersionDescription
        )
        handleMetadata(
            Const.MetadataGroup.defaultGroup,
            ModelData::gameDataServerRegion.name,
            data.gameDataServerRegion
        )
        handleMetadata(
            Const.MetadataGroup.defaultGroup,
            ModelData::arkPetsCompatibility.name,
            data.arkPetsCompatibility.joinToString(".")
        )
        metadataList.forEach { it.delete() }
    }

    @Suppress("LongMethod", "NestedBlockDepth")
    private fun handleModel(key: String, data: ModelConfig) {
        val md5 = data.md5(key)
        modelInfoList.getOrNull(data.assetId)?.let {
            if (it.md5 != md5) {
                it.md5 = md5
                it.assetId = data.assetId
                it.type = data.type
                it.style = data.style
                it.name = data.name
                it.appellation = data.appellation
                it.skinGroupId = data.skinGroupId
                it.skinGroupName = data.skinGroupName
                it.storePath = "${storageDirectory.getOrDefault(data.type, ".")}/$key"
                database.updateModelInfo(it)
            }
            modelInfoList.remove(it)
        } ?: run {
            val modelInfo = ModelInfo {
                this.md5 = md5
                this.assetId = data.assetId
                this.type = data.type
                this.style = data.style
                this.name = data.name
                this.appellation = data.appellation
                this.skinGroupId = data.skinGroupId
                this.skinGroupName = data.skinGroupName
                this.storePath = "${storageDirectory.getOrDefault(data.type, ".")}/$key"
            }
            database.addModelInfo(modelInfo)
            data.sortTags.forEach {
                database.addModelTags(
                    ModelTag {
                        this.modelInfo = modelInfo
                        this.tag = it
                    }
                )
            }
            data.assetList.forEach {
                when (it.value) {
                    is String -> {
                        database.addModelAssets(
                            ModelAsset {
                                this.modelInfo = modelInfo
                                this.type = it.key
                                this.filename = it.value as String
                            }
                        )
                    }

                    is List<*> -> {
                        (it.value as List<*>).forEach { filename ->
                            database.addModelAssets(
                                ModelAsset {
                                    this.modelInfo = modelInfo
                                    this.type = it.key
                                    this.filename = filename as String
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    fun init(jsonFileName: String) {
        val json = JSON.parseObject(File(jsonFileName).readText(), ModelData::class.java)
        storageDirectory = json.storageDirectory
        checkMetadata(json)
        json.data.forEach { handleModel(it.key, it.value) }
    }
}
