package eu.kanade.tachiyomi.megaplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class MegaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("megaApp", MegaAppExtension::class.java)

        project.tasks.register("generateMegaApp") {
            group = "mega-app"
            description = "Scans repository and automatically generates SourceRegistry, SourceSets, etc."

            doLast {
                val scanner = RepositoryScanner(project.rootDir)
                val repo = scanner.scan()
                println("MegaPlugin: Found ${repo.extensions.size} extensions, ${repo.multisrcThemes.size} multisrc themes, and ${repo.libs.size} libraries.")
                
                // Future phases will be called here
            }
        }
    }
}

open class MegaAppExtension {
    var generateDir: String = "build/generated/mega"
}
