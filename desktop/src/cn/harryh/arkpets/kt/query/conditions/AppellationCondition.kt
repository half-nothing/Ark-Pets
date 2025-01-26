package cn.harryh.arkpets.kt.query.conditions

import cn.harryh.arkpets.kt.database.tables.CharInfos
import org.ktorm.schema.ColumnDeclaring

class AppellationCondition(
    value: String,
    type: MatchConditionType = MatchConditionType.PRECISE_MATCH
) : MatchCondition(value, type) {
    override val property: ColumnDeclaring<String> = CharInfos.appellation
}
