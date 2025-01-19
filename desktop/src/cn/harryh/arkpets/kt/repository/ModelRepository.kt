package cn.harryh.arkpets.kt.repository

import cn.harryh.arkpets.Const.RepositoryConfig.ModelRepository
import cn.harryh.arkpets.kt.database.entity.Metadata
import cn.harryh.arkpets.kt.database.entity.ModelAsset
import cn.harryh.arkpets.kt.database.entity.CharInfo
import cn.harryh.arkpets.kt.database.entity.ModelTag
import cn.harryh.arkpets.kt.database.buffer.ModelAssetSqlBuffer
import cn.harryh.arkpets.kt.database.buffer.CharInfoSqlBuffer
import cn.harryh.arkpets.kt.database.buffer.ModelTagSqlBuffer
import cn.harryh.arkpets.kt.extension.md5
import cn.harryh.arkpets.kt.extension.metadata
import cn.harryh.arkpets.kt.extension.charInfo
import cn.harryh.arkpets.kt.extension.updateInfo
import cn.harryh.arkpets.kt.json.ModelConfig
import cn.harryh.arkpets.kt.json.ModelData
import com.alibaba.fastjson2.JSON
import org.ktorm.entity.map
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

object ModelRepository : Repository() {
    init {
        repoName = ModelRepository.repoName
        localPath = ModelRepository.repoPath
    }

    private val metadataCache: MutableMap<String, Metadata> = mutableMapOf()

    private val charInfoCache: MutableMap<String, CharInfo> = mutableMapOf()

    private lateinit var storageDirectory: Map<String, String>

    private val pattern = Pattern.compile("build_((char_\\d+_\\w+)([\\s\\S#]+)*)?", Pattern.CASE_INSENSITIVE)

    private val matcher: Matcher = pattern.matcher("")

    private val charInfoRepository: CharInfoSqlBuffer = CharInfoSqlBuffer(database)
    private val modelAssetRepository: ModelAssetSqlBuffer = ModelAssetSqlBuffer(database)
    private val modelTagRepository: ModelTagSqlBuffer = ModelTagSqlBuffer(database)

    override fun initRepository() {
        val json = JSON.parseObject(File(ModelRepository.metadataFilePath).readText(), ModelConfig::class.java)
        database.metadata.map {
            metadataCache.put("${it.repo}.${it.group}.${it.key}", it)
        }
        database.charInfo.map {
            charInfoCache.put(it.assetId, it)
        }
        storageDirectory = json.storageDirectory
        checkMetadata(json)
        checkModelData(json.data)
        executePendingOperations()
    }

    private fun executePendingOperations() {
        database.useTransaction {
            metadataRepository.executePendingOperations()
            charInfoRepository.executePendingOperations()
            modelAssetRepository.executePendingOperations()
            modelTagRepository.executePendingOperations()
        }
    }

    private fun handleMetadata(group: String, key: String, value: String) {
        metadataCache["${ModelRepository.repoName}.$group.$key"]?.let {
            if (it.value != value) {
                it.value = value
                updateMetaData(it)
            }
            metadataCache.remove(it.key)
            return
        }
        addMetaData(group, key, value)
    }

    private fun checkMetadata(data: ModelConfig) {
        data.sortTags.forEach { handleMetadata(ModelRepository.MetadataGroup.sortTagGroup, it.key, it.value) }
        handleMetadata(
            ModelRepository.MetadataGroup.defaultGroup,
            ModelConfig::gameDataVersionDescription.name,
            data.gameDataVersionDescription
        )
        handleMetadata(
            ModelRepository.MetadataGroup.defaultGroup,
            ModelConfig::gameDataServerRegion.name,
            data.gameDataServerRegion
        )
        handleMetadata(
            ModelRepository.MetadataGroup.defaultGroup,
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
        var charInfo = charInfoCache[data.assetId]
        if (charInfo != null) {
            charInfoCache.remove(charInfo.assetId)
            if (charInfo.md5 == md5) {
                return
            }
            charInfo.updateInfo(data, md5, storePath)
            updateCharInfo(charInfo)
            return
        }
        charInfo = CharInfo().updateInfo(data, md5, storePath)
        addCharInfo(charInfo)
        addModelTags(charInfo, data.sortTags)
        addModelAssets(charInfo, data.assetList)
    }

    private fun checkModelData(data: Map<String, ModelData>) {
        data.forEach { handleModel(it.key, it.value) }
        charInfoCache.forEach { it.value.delete() }
        charInfoCache.clear()
    }

    private fun addCharInfo(charInfo: CharInfo) {
        charInfoRepository.addInsert(charInfo)
    }

    private fun updateCharInfo(charInfo: CharInfo) {
        charInfoRepository.addUpdate(charInfo)
    }

    private fun addModelTags(charInfo: CharInfo, tags: List<String>) {
        tags.forEach {
            modelTagRepository.addInsert(
                ModelTag {
                    this.charInfo = charInfo
                    this.tag = it
                }
            )
        }
    }

    private fun addModelAssets(charInfo: CharInfo, assetMap: Map<String, Any>) {
        val assetList = mutableListOf<Pair<String, String>>()
        assetMap.forEach {
            if (it.value is String) {
                assetList.add(Pair(it.key, it.value as String))
                return@forEach
            }
            if (it.value is List<*>) {
                (it.value as List<*>).forEach { filename -> assetList.add(Pair(it.key, filename as String)) }
            }
        }
        addModelAssets(charInfo, assetList)
    }

    private fun addModelAssets(charInfo: CharInfo, assetList: List<Pair<String, String>>) {
        assetList.forEach { addModelAssets(charInfo, it.first, it.second) }
    }

    private fun addModelAssets(charInfo: CharInfo, key: String, value: String) {
        modelAssetRepository.addInsert(
            ModelAsset {
                this.charInfo = charInfo
                this.type = key
                this.filename = value
            }
        )
    }
}
