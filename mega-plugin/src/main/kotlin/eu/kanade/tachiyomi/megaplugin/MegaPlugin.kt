package eu.kanade.tachiyomi.megaplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class MegaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("megaApp", MegaAppExtension::class.java)

        val rootDir = project.rootDir
        
        project.tasks.register("generateMegaApp") {
            group = "mega-app"
            description = "Scans repository and automatically generates SourceRegistry, SourceSets, etc."

            doLast {
                val scanner = RepositoryScanner(rootDir)
                val repo = scanner.scan()
                println("MegaPlugin: Found ${repo.extensions.size} extensions, ${repo.multisrcThemes.size} multisrc themes, and ${repo.libs.size} libraries.")
                
                val metadataCollector = MetadataCollector()
                val dependencyResolver = DependencyResolver(metadataCollector)
                val resolvedDeps = dependencyResolver.resolve(repo)
                
                println("MegaPlugin: Resolved ${resolvedDeps.libs.size} required shared libraries.")
                resolvedDeps.libs.forEach { lib ->
                    println("  - Required library: ${lib.name}")
                }
                
                val sourceSetGenerator = SourceSetGenerator()
                sourceSetGenerator.generate(rootDir, repo, resolvedDeps)
                
                val registryGenerator = SourceRegistryGenerator()
                registryGenerator.generate(rootDir, repo)
            }
        }
    }
}

open class MegaAppExtension {
    var generateDir: String = "build/generated/mega"
}
