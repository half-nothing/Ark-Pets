package cn.harryh.arkpets.kt.database

import cn.harryh.arkpets.Const
import cn.harryh.arkpets.kt.extension.checkFolderExist
import cn.harryh.arkpets.utils.Logger
import org.ktorm.database.Database
import java.io.File
import java.io.FileNotFoundException
import java.sql.Connection
import java.sql.DriverManager
import kotlin.concurrent.thread

object DatabaseHelper {
    private val database: Database
    private val connection: Connection

    init {
        Const.PathConfig.dataDirPath.checkFolderExist(true)
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:${Const.PathConfig.databaseFilePath}")
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(thread(start = false) { connection.close() })
        database = Database.connect {
            // keep connection open
            object : Connection by connection {
                @Suppress("EmptyFunctionBlock")
                override fun close() {
                }
            }
        }
        try {
            val initSql = File(Const.PathConfig.initSqlFilename).readText(Charsets.UTF_8)
            database.useTransaction { transaction ->
                transaction.connection.createStatement().use {
                    it.executeUpdate(initSql)
                }
            }
        } catch (e: FileNotFoundException) {
            Logger.error(
                this.javaClass.simpleName,
                "Initialization sql file cannot be found, database may not have been successfully initialized.",
                e
            )
        }
    }

    fun getDatabase(): Database = database
}
