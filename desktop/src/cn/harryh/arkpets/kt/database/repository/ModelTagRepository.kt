package cn.harryh.arkpets.kt.database.repository

import cn.harryh.arkpets.kt.database.entity.ModelTag
import cn.harryh.arkpets.kt.database.model.ModelTags
import org.ktorm.database.Database
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class ModelTagRepository(database: Database) : Repository<ModelTag>(database) {
    override fun pendingInserts() {
        database.batchInsert(ModelTags) {
            pendingInserts.forEach { info ->
                item {
                    set(it.modelId, info.modelInfo.id)
                    set(it.tag, info.tag)
                }
            }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(ModelTags) {
            pendingUpdates.forEach { info ->
                item {
                    set(it.modelId, info.modelInfo.id)
                    set(it.tag, info.tag)
                    where { it.id eq info.id }
                }
            }
        }
    }
}
