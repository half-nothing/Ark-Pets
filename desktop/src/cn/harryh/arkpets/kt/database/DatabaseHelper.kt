package cn.harryh.arkpets.kt.database

import cn.harryh.arkpets.Const
import org.ktorm.database.Database
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object DatabaseHelper {
    private var database: Database
    private var connection: Connection

    init {
        val path = Path(Const.PathConfig.dataDirPath)
        if (!path.exists()) {
            path.createDirectories()
        }
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:${Const.PathConfig.databaseFilePath}")
//        connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        Runtime.getRuntime().addShutdownHook(thread(start = false) { connection.close() })
        database = Database.connect {
            object : Connection by connection {
                @Suppress("EmptyFunctionBlock")
                override fun close() {
                }
            }
        }
        val initSql = File(Const.PathConfig.initSqlFilename).readText(Charsets.UTF_8)
        database.useTransaction { transaction ->
            transaction.connection.createStatement().use {
                it.executeUpdate(initSql)
            }
        }
    }

    fun getDatabase(): Database = database
}
