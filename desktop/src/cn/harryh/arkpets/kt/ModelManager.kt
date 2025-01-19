package cn.harryh.arkpets.kt

import cn.harryh.arkpets.Const
import cn.harryh.arkpets.kt.database.DatabaseHelper
import cn.harryh.arkpets.kt.database.entity.Metadata
import cn.harryh.arkpets.kt.database.entity.ModelInfo
import cn.harryh.arkpets.kt.extension.md5
import cn.harryh.arkpets.kt.extension.metadata
import cn.harryh.arkpets.kt.extension.modelInfos
import cn.harryh.arkpets.kt.extension.updateInfo
import cn.harryh.arkpets.kt.model.ModelConfig
import cn.harryh.arkpets.kt.model.ModelData
import com.alibaba.fastjson2.JSON
import org.ktorm.entity.map
import org.ktorm.entity.toList
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

object ModelManager {
    private val database = DatabaseHelper.getDatabase()

    private val metadataCache: MutableMap<String, Metadata> = mutableMapOf()

    private val modelInfoCache: MutableMap<String, ModelInfo> = mutableMapOf()

    private lateinit var storageDirectory: Map<String, String>

    private val pattern = Pattern.compile("build_((char_\\d+_\\w+)([\\s\\S#]+)*)?", Pattern.CASE_INSENSITIVE)

    private val matcher: Matcher = pattern.matcher("")

    private fun handleMetadata(group: String, key: String, value: String) {
        metadataCache["$group.$key"]?.let {
            if (it.value != value) {
                it.value = value
                DatabaseHelper.updateMetaData(it)
            }
            metadataCache.remove(it.key)
            return
        }
        DatabaseHelper.addMetaData(group, key, value)
    }

    private fun checkMetadata(data: ModelConfig) {
        data.sortTags.forEach { handleMetadata(Const.MetadataGroup.sortTagGroup, it.key, it.value) }
        handleMetadata(
            Const.MetadataGroup.defaultGroup,
            ModelConfig::gameDataVersionDescription.name,
            data.gameDataVersionDescription
        )
        handleMetadata(
            Const.MetadataGroup.defaultGroup,
            ModelConfig::gameDataServerRegion.name,
            data.gameDataServerRegion
        )
        handleMetadata(
            Const.MetadataGroup.defaultGroup,
            ModelConfig::arkPetsCompatibility.name,
            data.arkPetsCompatibility.joinToString(".")
        )
        metadataCache.forEach { it.value.delete() }
        metadataCache.clear()
    }

    private fun handleModel(key: String, data: ModelData) {
        val md5 = data.md5(key)
        val storePath = "${storageDirectory.getOrDefault(data.type, ".")}/$key"
        matcher.reset(data.assetId)
        if (matcher.find() && matcher.groupCount() >= 2) {
            data.assetId = matcher.group(1)
        }
        var modelInfo = modelInfoCache[data.assetId]
        if (modelInfo != null) {
            modelInfoCache.remove(modelInfo.assetId)
            if (modelInfo.md5 == md5) {
                return
            }
            modelInfo.updateInfo(data, md5, storePath)
            DatabaseHelper.updateModelInfo(modelInfo)
            return
        }
        modelInfo = ModelInfo().updateInfo(data, md5, storePath)
        DatabaseHelper.addModelInfo(modelInfo)
        DatabaseHelper.addModelTags(modelInfo, data.sortTags)
        DatabaseHelper.addModelAssets(modelInfo, data.assetList)
    }

    private fun checkModelData(data: Map<String, ModelData>) {
        data.forEach { handleModel(it.key, it.value) }
        modelInfoCache.forEach { it.value.delete() }
        modelInfoCache.clear()
    }

    fun init(jsonFileName: String) {
        val json = JSON.parseObject(File(jsonFileName).readText(), ModelConfig::class.java)
        database.metadata.map {
            metadataCache.put("${it.group}.${it.key}", it)
        }
        database.modelInfos.map {
            modelInfoCache.put(it.assetId, it)
        }
        storageDirectory = json.storageDirectory
        checkMetadata(json)
        checkModelData(json.data)
        DatabaseHelper.executePendingOperations()
    }

    fun getAllModels(): List<ModelInfo> = database.modelInfos.toList()
}
