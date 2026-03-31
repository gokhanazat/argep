package com.argesurec.shared.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DatabaseDriverFactory {
    // This is a placeholder for wasmJs - final implementation requires deeper setup
    actual fun createDriver(): SqlDriver {
        return WebWorkerDriver(
            Worker("sqldelight-worker.js")
        )
    }
}
