plugins {
    alias(libs.plugins.android.application)
    id("eu.kanade.tachiyomi.mega-plugin")
}

android {
    namespace = "eu.kanade.tachiyomi.megaextension"
    compileSdk = 34

    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.megaextension"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
}

dependencies {
    implementation(project(":core"))
}

// Dependencies and SourceSets are automatically managed by eu.kanade.tachiyomi.mega-plugin

val extensionProjects = rootProject.subprojects.filter { proj ->
    proj.path.startsWith(":src:") && proj.path.count { it == ':' } == 3
}.map { proj ->
    val lang = proj.parent?.name ?: "en"
    val ext = proj.name
    val gradleFile = proj.file("build.gradle.kts")
    Triple(lang, ext, gradleFile)
}

val generateMegaFactory = tasks.register("generateMegaFactory") {
    val outDir = layout.buildDirectory.dir("generated/source/mega").get().asFile
    outputs.dir(outDir)
    
    val extProjects = extensionProjects
    
    doLast {
        val factoryFile = File(outDir, "eu/kanade/tachiyomi/megaextension/MegaExtensionFactory.kt")
        factoryFile.parentFile.mkdirs()
        
        val code = StringBuilder()
        code.appendLine("package eu.kanade.tachiyomi.megaextension")
        code.appendLine()
        code.appendLine("import eu.kanade.tachiyomi.source.SourceFactory")
        code.appendLine("import eu.kanade.tachiyomi.source.Source")
        code.appendLine()
        code.appendLine("class MegaExtensionFactory : SourceFactory {")
        code.appendLine("    override fun createSources(): List<Source> {")
        code.appendLine("        val sources = mutableListOf<Source>()")
        
        for ((lang, ext, gradleFile) in extProjects) {
            if (gradleFile.exists()) {
                val content = gradleFile.readText()
                var classname = ""
                val classMatch = Regex("className\\s*=\\s*\"([^\"]+)\"").find(content)
                if (classMatch != null) {
                    classname = classMatch.groupValues[1]
                }
                
                val fqn = if (classname.isNotEmpty()) {
                    "eu.kanade.tachiyomi.extension.\$lang.\$ext.\$classname"
                } else {
                    "eu.kanade.tachiyomi.extension.\$lang.\$ext.ExtensionGenerated"
                }
                
                code.appendLine("        try {")
                code.appendLine("            val instance = Class.forName(\"\$fqn\").getDeclaredConstructor().newInstance()")
                code.appendLine("            if (instance is SourceFactory) {")
                code.appendLine("                sources.addAll(instance.createSources())")
                code.appendLine("            } else if (instance is Source) {")
                code.appendLine("                sources.add(instance)")
                code.appendLine("            }")
                code.appendLine("        } catch (e: Exception) {")
                code.appendLine("            // Ignore")
                code.appendLine("        }")
            }
        }
        
        code.appendLine("        return sources")
        code.appendLine("    }")
        code.appendLine("}")
        
        factoryFile.writeText(code.toString())
    }
}

android.sourceSets.named("main") {
    java.directories.add(layout.buildDirectory.dir("generated/source/mega").get().asFile.absolutePath)
}

tasks.whenTaskAdded {
    if (name.startsWith("compileDebugKotlin") || name.startsWith("compileReleaseKotlin")) {
        dependsOn(generateMegaFactory)
    }
}


