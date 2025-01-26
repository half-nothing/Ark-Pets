package cn.harryh.arkpets.kt.query.conditions

import cn.harryh.arkpets.kt.database.tables.CharInfos
import org.ktorm.dsl.eq
import org.ktorm.schema.ColumnDeclaring

class IdCondition(private val id: String) : Condition {
    override fun applyCondition(): ColumnDeclaring<Boolean> = CharInfos.assetId eq id
}
