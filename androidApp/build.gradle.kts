import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    id("com.google.gms.google-services")
}

val configuredLoanApiBaseUrl = providers.gradleProperty("loanApiBaseUrl")
    .orElse(providers.environmentVariable("LOAN_API_BASE_URL"))
    .orElse("https://api.example.com/")
    .get()
    .let { if (it.endsWith('/')) it else "$it/" }

kotlin {

    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")


    // Converter library to handle JSON serialization/deserialization
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation(project(":shared"))
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation(libs.androidx.activity.compose)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.compose.uiToolingPreview)
    // ViewModel Compose integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Lifecycle runtime integration (for collectAsStateWithLifecycle)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    debugImplementation(libs.compose.uiTooling)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
}

android {
    namespace = "com.example.kotlinmulti"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.kotlinmulti"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "LOAN_API_BASE_URL", "\"$configuredLoanApiBaseUrl\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.configureEach {
    if (name.contains("Release", ignoreCase = true) &&
        (name.startsWith("assemble") || name.startsWith("bundle"))
    ) {
        doFirst {
            require(configuredLoanApiBaseUrl.startsWith("https://", ignoreCase = true)) {
                "Release builds require an HTTPS loanApiBaseUrl. Set -PloanApiBaseUrl=https://your-api-host/"
            }
            require(!configuredLoanApiBaseUrl.contains("api.example.com", ignoreCase = true)) {
                "Set the production API host with -PloanApiBaseUrl=https://your-api-host/ before building release."
            }
        }
    }
}
