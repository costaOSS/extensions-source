plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // For proper Kotlin analysis without Regex
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.23")
    implementation("com.squareup:kotlinpoet:1.16.0")
}

gradlePlugin {
    plugins {
        create("megaPlugin") {
            id = "eu.kanade.tachiyomi.mega-plugin"
            implementationClass = "eu.kanade.tachiyomi.megaplugin.MegaPlugin"
        }
    }
}

