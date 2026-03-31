package com.argesurec.shared.db

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

class Database(driverFactory: DatabaseDriverFactory) {
    private val database = ArgepDb(driverFactory.createDriver())
    val dbQueries = database.argepDbQueries
}
