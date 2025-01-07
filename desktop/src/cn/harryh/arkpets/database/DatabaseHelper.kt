package cn.harryh.arkpets.database

import cn.harryh.arkpets.Const
import org.ktorm.database.Database
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object DatabaseHelper {
    private var database: Database

    init {
        val path = Path(Const.PathConfig.dataDirPath)
        if (!path.exists()) {
            path.createDirectories()
        }
        database = Database.connect(
            url = "jdbc:sqlite:${Const.PathConfig.databaseFilePath}",
            driver = "org.sqlite.JDBC"
        )
        val initSql = File(Const.PathConfig.initSqlFilename).readText(Charsets.UTF_8)
        database.useTransaction { transaction ->
            transaction.connection.createStatement().use {
                it.executeUpdate(initSql)
            }
        }
    }

    fun getDatabase(): Database = database
}
