package cn.harryh.arkpets.kt.query.conditions

import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.schema.ColumnDeclaring

abstract class MatchCondition(
    private val value: String,
    private val type: MatchConditionType = MatchConditionType.PRECISE_MATCH
) : Condition {
    protected abstract val property: ColumnDeclaring<String>

    override fun applyCondition(): ColumnDeclaring<Boolean> {
        return when (type) {
            MatchConditionType.PRECISE_MATCH -> {
                property eq value
            }

            MatchConditionType.FUZZY_MATCH -> {
                property like "%$value%"
            }

            MatchConditionType.FULL_FUZZY_MATCH -> {
                val pattern = StringBuilder()
                value.chars().forEach {
                    pattern.append("%").append(it.toChar())
                }
                pattern.append("%")
                property like pattern.toString()
            }
        }
    }
}
