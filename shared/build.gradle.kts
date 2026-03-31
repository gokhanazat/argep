import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight.plugin)
}

val properties = Properties()
val propertiesFile = project.rootProject.file("local.properties")
if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

sqldelight {
    databases {
        create("ArgepDb") {
            packageName.set("com.argesurec.shared.db")
        }
    }
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "shared.js"
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.functions)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
        }

        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android)
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.sqldelight.web)
            }
        }
    }
}

android {
    namespace = "com.argesurec.shared"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24
        val supabaseUrl = properties.getProperty("SUPABASE_URL")?.removeSurrounding("\"") ?: ""
        val supabaseKey = properties.getProperty("SUPABASE_ANON_KEY")?.removeSurrounding("\"") ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// WASM Environment Variable Injection Task
tasks.register("injectWasmEnv") {
    doFirst {
        val supabaseUrl = System.getenv("SUPABASE_URL") ?: properties.getProperty("SUPABASE_URL")?.removeSurrounding("\"") ?: ""
        val supabaseKey = System.getenv("SUPABASE_ANON_KEY") ?: properties.getProperty("SUPABASE_ANON_KEY")?.removeSurrounding("\"") ?: ""
        
        val configFile = file("src/wasmJsMain/kotlin/com/argesurec/shared/SupabaseConfig.kt")
        if (configFile.exists()) {
            var content = configFile.readText()
            content = content.replace("SUPABASE_URL_PLACEHOLDER", supabaseUrl)
            content = content.replace("SUPABASE_ANON_KEY_PLACEHOLDER", supabaseKey)
            configFile.writeText(content)
            println("WASM Environment variables injected successfully into ${configFile.name}")
        }
    }
}

// Her derlemeden önce enjeksiyonu yap
tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile>().configureEach {
    dependsOn("injectWasmEnv")
}
