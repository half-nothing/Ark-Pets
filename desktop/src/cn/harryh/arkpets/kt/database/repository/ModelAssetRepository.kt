package cn.harryh.arkpets.kt.database.repository

import cn.harryh.arkpets.kt.database.entity.ModelAsset
import cn.harryh.arkpets.kt.database.model.ModelAssets
import org.ktorm.database.Database
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class ModelAssetRepository(database: Database) : Repository<ModelAsset>(database) {

    override fun pendingInserts() {
        database.batchInsert(ModelAssets) {
            pendingInserts.forEach { info ->
                item {
                    set(it.modelId, info.modelInfo.id)
                    set(it.type, info.type)
                    set(it.filename, info.filename)
                }
            }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(ModelAssets) {
            pendingUpdates.forEach { info ->
                item {
                    set(it.modelId, info.modelInfo.id)
                    set(it.type, info.type)
                    set(it.filename, info.filename)
                    where { it.id eq info.id }
                }
            }
        }
    }
}
