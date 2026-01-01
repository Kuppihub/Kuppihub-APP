import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

kotlin {
    // 👇 FIX 1: Simplified androidTarget to remove deprecation warning
    androidTarget {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.play.services.auth)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.materialIconsExtended)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.v111)
            implementation(libs.kotlinx.serialization.json.v160)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.common)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
            implementation(libs.firebase.admin)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.core)
            implementation(libs.google.auth.library.oauth2.http)
        }
    }
}

android {
    namespace = "org.kuppihub.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    // 👇 FIX 2: Renamed local variables (added 'env' prefix) to avoid shadowing errors
    signingConfigs {
        create("release") {
            val envStoreFile = System.getenv("SIGNING_STORE_FILE")
            val envStorePassword = System.getenv("SIGNING_STORE_PASSWORD")
            val envKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
            val envKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")

            if (envStoreFile != null && envStorePassword != null) {
                storeFile = file(envStoreFile)
                storePassword = envStorePassword
                keyAlias = envKeyAlias        // Now this works!
                keyPassword = envKeyPassword  // Now this works!
            } else {
                println("⚠️ Warning: Release signing keys not found in Environment Variables.")
            }
        }
    }

    defaultConfig {
        applicationId = "org.kuppihub.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.kuppihub.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.kuppihub.app"
            packageVersion = "1.0.0"
        }
    }
}