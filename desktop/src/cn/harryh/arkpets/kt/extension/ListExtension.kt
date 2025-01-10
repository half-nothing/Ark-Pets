package cn.harryh.arkpets.kt.extension

import cn.harryh.arkpets.kt.database.DatabaseHelper
import cn.harryh.arkpets.kt.database.entity.Metadata
import cn.harryh.arkpets.kt.database.entity.ModelInfo
import org.ktorm.database.Database

val database: Database by lazy {
    DatabaseHelper.getDatabase()
}

fun List<Metadata>.getOrNull(key: String): Metadata? {
    val filteredList = filter { it.key == key }
    if (filteredList.isEmpty()) return null
    return filteredList[0]
}

fun List<ModelInfo>.getOrNull(assetId: String): ModelInfo? {
    val filteredList = filter { it.assetId == assetId }
    if (filteredList.isEmpty()) return null
    return filteredList[0]
}
