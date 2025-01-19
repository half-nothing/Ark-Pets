package cn.harryh.arkpets.kt.database.repository

import cn.harryh.arkpets.kt.database.entity.ModelInfo
import cn.harryh.arkpets.kt.database.model.ModelInfos
import org.ktorm.database.Database
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class ModelInfoRepository(database: Database) : Repository<ModelInfo>(database) {
    override fun pendingInserts() {
        database.batchInsert(ModelInfos) {
            pendingInserts.forEach { info -> item { setData(it, info) } }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(ModelInfos) {
            pendingUpdates.forEach { info ->
                item {
                    setData(it, info)
                    where { it.id eq info.id }
                }
            }
        }
    }

    private fun AssignmentsBuilder.setData(it: ModelInfos, info: ModelInfo) {
        set(it.assetId, info.assetId)
        set(it.storePath, info.storePath)
        set(it.type, info.type)
        set(it.style, info.style)
        set(it.name, info.name)
        set(it.appellation, info.appellation)
        set(it.skinGroupId, info.skinGroupId)
        set(it.skinGroupName, info.skinGroupName)
        set(it.md5, info.md5)
    }
}
