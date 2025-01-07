package cn.harryh.arkpets.database.model

import cn.harryh.arkpets.database.entity.ModelInfo
import org.ktorm.dsl.isNotNull
import org.ktorm.dsl.isNull
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object ModelInfos : Table<ModelInfo>("model_info") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val assetId = varchar("asset_id").bindTo { it.assetId }.isNotNull()
    val storePath = varchar("store_path").bindTo { it.storePath }.isNotNull()
    val type = varchar("type").bindTo { it.type }.isNotNull()
    val style = varchar("style").bindTo { it.style }.isNull()
    val name = varchar("name").bindTo { it.name }.isNotNull()
    val appellation = varchar("appellation").bindTo { it.appellation }.isNull()
    val skinGroupId = varchar("skin_group_id").bindTo { it.skinGroupId }.isNotNull()
    val skinGroupName = varchar("skin_group_name").bindTo { it.skinGroupName }.isNotNull()
    val md5 = varchar("md5").bindTo { it.md5 }.isNotNull()
}
