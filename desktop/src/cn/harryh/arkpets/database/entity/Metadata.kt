package cn.harryh.arkpets.database.entity

import org.ktorm.entity.Entity

interface Metadata : Entity<Metadata> {
    var id: Int
    var group: String
    var key: String
    var value: String

    companion object : Entity.Factory<Metadata>()
}
