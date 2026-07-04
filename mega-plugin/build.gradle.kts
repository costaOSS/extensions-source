plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // We are using a custom lexer instead of embeddable compiler to avoid NoClassDefFoundError for OpenAPI classes
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

