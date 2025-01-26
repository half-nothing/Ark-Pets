package cn.harryh.arkpets.kt.query.conditions

import cn.harryh.arkpets.kt.database.tables.ModelTags
import org.ktorm.dsl.inList
import org.ktorm.schema.ColumnDeclaring

class TagsCondition(private val tags: List<String>) : Condition {
    override fun applyCondition(): ColumnDeclaring<Boolean> = ModelTags.tag inList tags
}
