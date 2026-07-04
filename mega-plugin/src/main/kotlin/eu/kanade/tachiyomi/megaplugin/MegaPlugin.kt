package eu.kanade.tachiyomi.megaplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class MegaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("megaApp", MegaAppExtension::class.java)

        val rootDir = project.rootDir
        
        // Phase 1 & 2 & 3: Run fast scanning during configuration to hook SourceSets
        val scanner = RepositoryScanner(rootDir)
        val repo = scanner.scan()
        val metadataCollector = MetadataCollector()
        val dependencyResolver = DependencyResolver(metadataCollector)
        val resolvedDeps = dependencyResolver.resolve(repo)
        
        val sourceSetGenerator = SourceSetGenerator()
        sourceSetGenerator.generate(project, repo, resolvedDeps)
        
        project.tasks.register("generateMegaApp") {
            group = "mega-app"
            description = "Scans repository and automatically generates SourceRegistry, SourceSets, etc."

            doLast {
                println("MegaPlugin: Found ${repo.extensions.size} extensions, ${repo.multisrcThemes.size} multisrc themes, and ${repo.libs.size} libraries.")
                println("MegaPlugin: Resolved ${resolvedDeps.libs.size} required shared libraries.")
                resolvedDeps.libs.forEach { lib ->
                    println("  - Required library: ${lib.name}")
                }
            }
        }
    }
}

open class MegaAppExtension {
    var generateDir: String = "build/generated/mega"
}
