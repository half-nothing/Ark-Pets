package cn.harryh.arkpets.kt.query.conditions

import org.ktorm.schema.ColumnDeclaring

interface Condition {
    fun applyCondition(): ColumnDeclaring<Boolean>
}
