package cn.harryh.arkpets.kt.query.conditions

import cn.harryh.arkpets.kt.database.tables.CharInfos
import org.ktorm.dsl.eq
import org.ktorm.schema.ColumnDeclaring

class TypeCondition(private val type: String) : Condition {
    override fun applyCondition(): ColumnDeclaring<Boolean> = CharInfos.type eq type
}
