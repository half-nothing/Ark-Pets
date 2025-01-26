package cn.harryh.arkpets.kt.query.builders

import cn.harryh.arkpets.kt.database.tables.CharInfos
import cn.harryh.arkpets.kt.database.tables.ModelTags
import cn.harryh.arkpets.kt.extension.groupConcat
import cn.harryh.arkpets.kt.json.ModelData
import cn.harryh.arkpets.kt.query.conditions.*
import org.ktorm.dsl.*

class CharInfoQueryBuilder : QueryBuilder() {
    private var idCondition: IdCondition? = null
    private var nameCondition: NameCondition? = null
    private var tagsCondition: TagsCondition? = null

    fun addIdCondition(id: String): CharInfoQueryBuilder {
        idCondition = IdCondition(id)
        return this
    }

    fun addNameCondition(name: String): CharInfoQueryBuilder {
        nameCondition = NameCondition(name)
        return this
    }

    fun addNameCondition(name: String, type: MatchConditionType): CharInfoQueryBuilder {
        nameCondition = NameCondition(name, type)
        return this
    }

    fun addTagCondition(tags: List<String>): CharInfoQueryBuilder {
        tagsCondition = TagsCondition(tags)
        return this
    }

    fun addTagCondition(vararg tags: String): CharInfoQueryBuilder {
        tagsCondition = TagsCondition(tags.toList())
        return this
    }

    override fun build(): Query {
        return database
            .from(CharInfos)
            .innerJoin(ModelTags, CharInfos.assetId eq ModelTags.modelId)
            .select(
                CharInfos.assetId,
                CharInfos.name,
                CharInfos.type,
                CharInfos.style,
                CharInfos.appellation,
                CharInfos.skinGroupId,
                CharInfos.skinGroupName,
                groupConcat(ModelTags.tag).aliased("tags")
            )
            .where {
                listOfNotNull(idCondition, nameCondition, tagsCondition)
                    .map(Condition::applyCondition)
                    .reduce { acc, condition -> acc and condition }
            }
            .groupBy(CharInfos.id)
    }

    override fun buildAndGetResult(): List<ModelData> = getQueryResult(build())

    companion object {
        @JvmStatic
        fun getQueryResult(query: Query): List<ModelData> {
            val result = mutableListOf<ModelData>()
            for (entity in query) {
                result.add(
                    ModelData(
                        entity[CharInfos.assetId]!!,
                        entity[CharInfos.type]!!,
                        entity[CharInfos.style],
                        entity[CharInfos.name]!!,
                        entity[CharInfos.appellation],
                        entity[CharInfos.skinGroupId]!!,
                        entity[CharInfos.skinGroupName]!!,
                        entity.getString("tags")!!.split(","),
                        mapOf()
                    )
                )
            }
            return result.toList()
        }
    }
}
