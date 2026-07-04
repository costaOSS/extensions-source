package eu.kanade.tachiyomi.megaplugin

import org.gradle.api.Project
import java.io.File

class RepositoryScanner(private val rootDir: File) {

    fun scan(): ScannedRepository {
        val extensions = mutableListOf<File>()
        val multisrcThemes = mutableListOf<File>()
        val libs = mutableListOf<File>()

        // Scan lib/
        val libDir = File(rootDir, "lib")
        if (libDir.exists() && libDir.isDirectory) {
            libDir.listFiles()?.filter { it.isDirectory && File(it, "build.gradle.kts").exists() }?.forEach {
                libs.add(it)
            }
        }

        // Scan lib-multisrc/
        val multisrcDir = File(rootDir, "lib-multisrc")
        if (multisrcDir.exists() && multisrcDir.isDirectory) {
            multisrcDir.listFiles()?.filter { it.isDirectory && File(it, "build.gradle.kts").exists() }?.forEach {
                multisrcThemes.add(it)
            }
        }

        // Scan src/*/
        val srcDir = File(rootDir, "src")
        if (srcDir.exists() && srcDir.isDirectory) {
            srcDir.listFiles()?.filter { it.isDirectory }?.forEach { langDir ->
                langDir.listFiles()?.filter { it.isDirectory && File(it, "build.gradle.kts").exists() }?.forEach { extDir ->
                    extensions.add(extDir)
                }
            }
        }

        return ScannedRepository(extensions, multisrcThemes, libs)
    }
}

data class ScannedRepository(
    val extensions: List<File>,
    val multisrcThemes: List<File>,
    val libs: List<File>
)
