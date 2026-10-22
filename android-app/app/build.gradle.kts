plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python")
}

android {
    namespace = "com.superstar.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.superstar.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 6
        versionName = "1.5.0"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

chaquopy {
    defaultConfig {
        version = "3.12"
        extractPackages("resource")
        val userHome = System.getProperty("user.home").replace("\\", "/")
        val uvPython = file("$userHome/AppData/Roaming/uv/python/cpython-3.12.14-windows-x86_64-none/python.exe")
        val standardPython312 = file("C:/Program Files/Python312/python.exe")
        if (uvPython.exists()) {
            buildPython(uvPython.absolutePath)
        } else if (standardPython312.exists()) {
            buildPython(standardPython312.absolutePath)
        }
        pip {
            install("requests")
            install("beautifulsoup4")
            // fonttools: 用于解析超星学习通加密字体，解码乱码题目
            install("fonttools")
            // pyaes 已直接内置于 src/main/python/pyaes 中，无需走 pip
            // 纯 Python 依赖，无任何 C 扩展二进制，确保 100% 兼容 16 KB 内存分页设备与最新 Android 17 系统
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
