package com.argesurec.shared

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.plugins.HttpTimeout
import io.github.jan.supabase.annotations.SupabaseInternal
import org.koin.dsl.module

@OptIn(SupabaseInternal::class)
fun createSupabaseClient(supabaseUrl: String, supabaseKey: String): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(Postgrest)
        install(Auth)
        install(Realtime)
        install(Functions)
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 15000
            }
        }
    }
}

fun supabaseModule(url: String, key: String) = module {
    single { createSupabaseClient(url, key) }
}

val repositoryModule = module {
    single<com.argesurec.shared.repository.ProjectRepository> { 
        com.argesurec.shared.repository.impl.SupabaseProjectRepository(get()) 
    }
    single<com.argesurec.shared.repository.TaskRepository> { 
        com.argesurec.shared.repository.impl.SupabaseTaskRepository(get()) 
    }
    single<com.argesurec.shared.repository.TeamRepository> { 
        com.argesurec.shared.repository.impl.SupabaseTeamRepository(get()) 
    }
    single<com.argesurec.shared.repository.MilestoneRepository> { 
        com.argesurec.shared.repository.impl.SupabaseMilestoneRepository(get()) 
    }
}

val viewModelModule = module {
    factory { com.argesurec.shared.viewmodel.AuthViewModel(get()) }
    factory { com.argesurec.shared.viewmodel.ProjectsViewModel(get(), get()) }
    factory { com.argesurec.shared.viewmodel.MilestoneViewModel(get()) }
    factory { com.argesurec.shared.viewmodel.TaskViewModel(get()) }
    factory { com.argesurec.shared.viewmodel.TeamViewModel(get(), get()) }
    single { com.argesurec.shared.viewmodel.SettingsViewModel() }
    factory { com.argesurec.shared.viewmodel.ReportsViewModel(get(), get(), get()) }
}
