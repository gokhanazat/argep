package com.argesurec.shared

import com.argesurec.shared.db.Database
import com.argesurec.shared.db.DatabaseDriverFactory
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

import org.koin.core.module.Module

expect val platformModule: Module

fun initKoin(
    supabaseUrl: String,
    supabaseKey: String,
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
        appDeclaration()
        modules(
            supabaseModule(supabaseUrl, supabaseKey),
            databaseModule,
            repositoryModule,
            viewModelModule,
            platformModule
        )
    }

val databaseModule = module {
    single { Database(get()) }
}

