package cn.harryh.arkpets.kt.database.repository

import cn.harryh.arkpets.kt.database.entity.Metadata
import cn.harryh.arkpets.kt.database.model.MetadataList
import org.ktorm.database.Database
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class MetadataRepository(database: Database) : Repository<Metadata>(database) {
    override fun pendingInserts() {
        database.batchInsert(MetadataList) {
            pendingInserts.forEach { info ->
                item {
                    set(it.group, info.group)
                    set(it.key, info.key)
                    set(it.value, info.value)
                }
            }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(MetadataList) {
            pendingUpdates.forEach { info ->
                item {
                    set(it.group, info.group)
                    set(it.key, info.key)
                    set(it.value, info.value)
                    where { it.id eq info.id }
                }
            }
        }
    }
}
