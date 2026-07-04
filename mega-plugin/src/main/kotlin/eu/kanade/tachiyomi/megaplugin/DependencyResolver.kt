package eu.kanade.tachiyomi.megaplugin

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import java.io.File

class DependencyResolver(private val metadataCollector: MetadataCollector) {

    fun resolve(repo: ScannedRepository): ResolvedDependencies {
        val requiredLibs = mutableSetOf<File>()
        
        // Build a map of library package -> library directory
        // Example: keiyoushi.lib.randomua -> lib/randomua
        val libMap = mutableMapOf<String, File>()
        for (libDir in repo.libs) {
            val libName = libDir.name
            // We can guess the package by scanning its own files or assuming keiyoushi.lib.$libName
            libMap["keiyoushi.lib.\$libName"] = libDir
            libMap["eu.kanade.tachiyomi.lib.\$libName"] = libDir
            libMap["eu.kanade.tachiyomi.network.interceptor"] = libDir // Some interceptors might use this
        }

        fun scanFileForImports(file: File) {
            val ktFile = metadataCollector.createKtFile(file)
            ktFile.accept(object : KtTreeVisitorVoid() {
                override fun visitImportDirective(importDirective: KtImportDirective) {
                    super.visitImportDirective(importDirective)
                    val importPath = importDirective.importPath?.pathStr ?: return
                    
                    // Check if it matches any library
                    for ((pkg, libDir) in libMap) {
                        if (importPath.startsWith(pkg)) {
                            requiredLibs.add(libDir)
                        }
                    }
                }
            })
        }

        // Scan extensions
        for (extDir in repo.extensions) {
            val srcFolder = File(extDir, "src")
            if (srcFolder.exists()) {
                srcFolder.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    scanFileForImports(file)
                }
            }
        }

        // Scan multisrc
        for (themeDir in repo.multisrcThemes) {
            val srcFolder = File(themeDir, "src")
            if (srcFolder.exists()) {
                srcFolder.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    scanFileForImports(file)
                }
            }
        }

        return ResolvedDependencies(requiredLibs.toList())
    }
}

data class ResolvedDependencies(
    val libs: List<File>
)
