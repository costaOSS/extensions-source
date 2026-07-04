plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    id("eu.kanade.tachiyomi.mega-plugin")
}

android {
    namespace = "eu.kanade.tachiyomi.megaextension"
    compileSdk = 36

    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.megaextension"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.4.1"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("signingkey.jks")
            storePassword = providers.environmentVariable("KEY_STORE_PASSWORD").orNull
            keyAlias = providers.environmentVariable("ALIAS").orNull
            keyPassword = providers.environmentVariable("KEY_PASSWORD").orNull
        }
    }

    buildTypes {
        named("release") {
            signingConfig = if (rootProject.file("signingkey.jks").exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-parameters")
    }
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.bundles.common)
    implementation(libs.kotlin.json)

    // Required by lib/randomua, lib-multisrc/kemono, lib-multisrc/natsuid
    implementation("com.squareup.okhttp3:okhttp-brotli:5.3.2")
    // Required by lib-multisrc/mangahub
    implementation("org.brotli:dec:0.1.2")
    // Required by lib/e4p
    implementation("org.kotlincrypto.hash:blake2:0.8.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
    // Required by lib/zipinterceptor
    implementation("com.github.tachiyomiorg:image-decoder:e08e9be535")
}

// Apply dynamic source sets
val sourcesFile = file("mega-sources.gradle")
if (sourcesFile.exists()) {
    apply(from = "mega-sources.gradle")
} else {
    println("mega-sources.gradle not found! Please run `./gradlew generateMegaApp` first.")
}

