package com.argesurec.shared

actual object SupabaseConfig {
    actual val URL: String = BuildConfig.SUPABASE_URL
    actual val ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY
}
