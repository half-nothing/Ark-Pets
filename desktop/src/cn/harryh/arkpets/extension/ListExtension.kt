package cn.harryh.arkpets.extension

import cn.harryh.arkpets.database.entity.Metadata
import cn.harryh.arkpets.database.entity.ModelInfo

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
