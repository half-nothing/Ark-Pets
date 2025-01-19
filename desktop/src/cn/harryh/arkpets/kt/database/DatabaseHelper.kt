package cn.harryh.arkpets.kt.database

import cn.harryh.arkpets.Const
import cn.harryh.arkpets.kt.database.entity.Metadata
import cn.harryh.arkpets.kt.database.entity.ModelAsset
import cn.harryh.arkpets.kt.database.entity.ModelInfo
import cn.harryh.arkpets.kt.database.entity.ModelTag
import cn.harryh.arkpets.kt.database.repository.MetadataRepository
import cn.harryh.arkpets.kt.database.repository.ModelAssetRepository
import cn.harryh.arkpets.kt.database.repository.ModelInfoRepository
import cn.harryh.arkpets.kt.database.repository.ModelTagRepository
import cn.harryh.arkpets.utils.Logger
import org.ktorm.database.Database
import java.io.File
import java.io.FileNotFoundException
import java.sql.Connection
import java.sql.DriverManager
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object DatabaseHelper {
    private lateinit var database: Database
    private var connection: Connection

    private val metadataRepository: MetadataRepository by lazy { MetadataRepository(database) }
    private val modelInfoRepository: ModelInfoRepository by lazy { ModelInfoRepository(database) }
    private val modelAssetRepository: ModelAssetRepository by lazy { ModelAssetRepository(database) }
    private val modelTagRepository: ModelTagRepository by lazy { ModelTagRepository(database) }

    init {
        val path = Path(Const.PathConfig.dataDirPath)
        if (!path.exists()) {
            path.createDirectories()
        }
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:${Const.PathConfig.databaseFilePath}")
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(thread(start = false) { connection.close() })
        database = Database.connect {
            // keep connection open
            object : Connection by connection {
                @Suppress("EmptyFunctionBlock")
                override fun close() {
                }
            }
        }
        try {
            val initSql = File(Const.PathConfig.initSqlFilename).readText(Charsets.UTF_8)
            database.useTransaction { transaction ->
                transaction.connection.createStatement().use {
                    it.executeUpdate(initSql)
                }
            }
        } catch (e: FileNotFoundException) {
            Logger.error(
                this.javaClass.simpleName,
                "Initialization sql file cannot be found, database may not have been successfully initialized.",
                e
            )
        }
    }

    fun getDatabase(): Database = database

    fun addMetaData(group: String, key: String, value: String) {
        metadataRepository.addInsert(
            Metadata {
                this.group = group
                this.key = key
                this.value = value
            }
        )
    }

    fun updateMetaData(metadata: Metadata) {
        metadataRepository.addUpdate(metadata)
    }

    fun addModelInfo(modelInfo: ModelInfo) {
        modelInfoRepository.addInsert(modelInfo)
    }

    fun updateModelInfo(modelInfo: ModelInfo) {
        modelInfoRepository.addUpdate(modelInfo)
    }

    fun addModelTags(modelInfo: ModelInfo, tags: List<String>) {
        tags.forEach {
            modelTagRepository.addInsert(
                ModelTag {
                    this.modelInfo = modelInfo
                    this.tag = it
                }
            )
        }
    }

    fun updateModelTags(modelTag: ModelTag) {
        modelTagRepository.addUpdate(modelTag)
    }

    fun addModelAssets(modelInfo: ModelInfo, assetMap: Map<String, Any>) {
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
        addModelAssets(modelInfo, assetList)
    }

    private fun addModelAssets(modelInfo: ModelInfo, assetList: List<Pair<String, String>>) {
        assetList.forEach { addModelAssets(modelInfo, it.first, it.second) }
    }

    private fun addModelAssets(modelInfo: ModelInfo, key: String, value: String) {
        modelAssetRepository.addInsert(
            ModelAsset {
                this.modelInfo = modelInfo
                this.type = key
                this.filename = value
            }
        )
    }

    fun updateModelAssets(modelAsset: ModelAsset) {
        modelAssetRepository.addInsert(modelAsset)
    }

    fun executePendingOperations() {
        database.useTransaction {
            metadataRepository.executePendingOperations()
            modelInfoRepository.executePendingOperations()
            modelAssetRepository.executePendingOperations()
            modelTagRepository.executePendingOperations()
        }
    }
}
