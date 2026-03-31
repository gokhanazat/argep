package com.argesurec.shared

actual object SupabaseConfig {
    actual val URL: String = getEnvUrl()
    actual val ANON_KEY: String = getEnvKey()
}

/**
 * Bu fonksiyonlar Webpack DefinePlugin ile inject edilen değerleri okur.
 * JS dünyasındaki `process.env.SUPABASE_URL` değerine erişir.
 */
private fun getEnvUrl(): String = js("process.env.SUPABASE_URL")
private fun getEnvKey(): String = js("process.env.SUPABASE_ANON_KEY")
