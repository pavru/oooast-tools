package net.pototskiy.apps.magemediation.database

import com.mysql.cj.jdbc.MysqlDataSource
import net.pototskiy.apps.magemediation.api.STATUS_LOG_NAME
import net.pototskiy.apps.magemediation.api.config.DatabaseConfig
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import java.util.*

fun initDatabase(config: DatabaseConfig, logLevel: Level = Level.ERROR) {
    val statusLog = LogManager.getLogger(STATUS_LOG_NAME)
    statusLog.info("Database has stated to check and init")
    val datasource = MysqlDataSource()
    datasource.setURL("jdbc:mysql://${config.server.host}:${config.server.port}/${config.name}")
    datasource.user = config.server.user
    datasource.password = config.server.password
    datasource.serverTimezone = TimeZone.getDefault().id

    try {
        Database.connect(datasource)
        DbSchema.createSchema()
    } catch (e: Exception) {
        statusLog.error("Can not init DB", e)
        System.exit(1)
    }
    statusLog.info("Database has finished to check and init")
}
