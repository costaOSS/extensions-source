package eu.kanade.tachiyomi.megaplugin

import org.gradle.api.Project
import java.io.File
import java.security.MessageDigest

class SourceRegistryGenerator {
    
    fun generate(project: Project, repo: ScannedRepository) {
        val megaAppSrcDir = File(project.rootDir, "mega-app/src/main/java/eu/kanade/tachiyomi/mega/generated")
        if (megaAppSrcDir.exists()) {
            megaAppSrcDir.deleteRecursively()
        }
        megaAppSrcDir.mkdirs()
        
        val sources = mutableListOf<String>()
        
        // 1. Process Extensions
        repo.extensions.forEach { ext ->
            val gradleFile = File(ext, "build.gradle.kts")
            if (gradleFile.exists()) {
                val content = gradleFile.readText()
                val name = extractRegex(content, Regex("name\\s*=\\s*\"([^\"]+)\"")) ?: return@forEach
                val lang = extractRegex(content, Regex("lang\\s*=\\s*\"([^\"]+)\"")) ?: "en"
                val baseUrl = extractRegex(content, Regex("baseUrl\\s*=\\s*\"([^\"]+)\"")) ?: ""
                val isMultisrc = content.contains("kei.plugins.multisrc")
                
                if (isMultisrc) {
                    val theme = extractRegex(content, Regex("theme\\s*=\\s*\"([^\"]+)\""))
                    val themeClass = extractRegex(content, Regex("themeClass\\s*=\\s*\"([^\"]+)\""))
                    val pkg = extractRegex(content, Regex("classPkg\\s*=\\s*\"([^\"]+)\""))
                    
                    if (theme != null && themeClass != null && pkg != null) {
                        val className = pkg + "." + themeClass
                        val id = computeSourceId(name, lang, 1)
                        val generatedClass = "ExtensionGenerated_${lang.replace(Regex("[^A-Za-z0-9]"), "_")}_${name.replace(Regex("[^A-Za-z0-9]"), "")}"
                        
                        val file = File(megaAppSrcDir, "$generatedClass.kt")
                        // Skip baseUrl override if it contains Groovy template variables like $it or $sub
                        val safeBaseUrl = if (baseUrl.contains("\$")) "" else baseUrl
                        val baseUrlLine = if (safeBaseUrl.isNotEmpty()) "    override val baseUrl = \"$safeBaseUrl\"" else ""
                        file.writeText("""
                            package eu.kanade.tachiyomi.mega.generated
                            
                            class $generatedClass : $className() {
                                override val name = "$name"
                                $baseUrlLine
                                override val lang = "$lang"
                                override val id = ${id}L
                            }
                        """.trimIndent())
                        sources.add(generatedClass)
                    }
                } else {
                    // Normal extension
                    val srcDir = File(ext, "src")
                    var pkg = ""
                    var clsName = ""
                    var isAbstract = false
                    
                    srcDir.walkTopDown().filter { it.extension == "kt" }.forEach { ktFile ->
                        val text = ktFile.readText()
                        if (text.contains("@Source")) {
                            val p = extractRegex(text, Regex("package\\s+([^\\s;]+)"))
                            val c = extractRegex(text, Regex("(?:abstract\\s+)?class\\s+([^\\s:]+)"))
                            val abs = text.contains("abstract class")
                            if (p != null && c != null) {
                                pkg = p
                                clsName = c
                                isAbstract = abs
                            }
                        }
                    }
                    
                    if (pkg.isNotEmpty() && clsName.isNotEmpty()) {
                        val id = computeSourceId(name, lang, 1)
                        val generatedClass = "ExtensionGenerated_${lang.replace(Regex("[^A-Za-z0-9]"), "_")}_${name.replace(Regex("[^A-Za-z0-9]"), "")}"
                        
                        if (isAbstract) {
                            val file = File(megaAppSrcDir, "$generatedClass.kt")
                            // Skip baseUrl override if it contains Groovy template variables like $it or $sub
                            val safeBaseUrl = if (baseUrl.contains("\$")) "" else baseUrl
                            val baseUrlLine = if (safeBaseUrl.isNotEmpty()) "    override val baseUrl = \"$safeBaseUrl\"" else ""
                            file.writeText("""
                                package eu.kanade.tachiyomi.mega.generated
                                
                                class $generatedClass : $pkg.$clsName() {
                                    override val name = "$name"
                                    $baseUrlLine
                                    override val lang = "$lang"
                                    override val id = ${id}L
                                }
                            """.trimIndent())
                            sources.add(generatedClass)
                        } else {
                            // If not abstract, just instantiate the class directly
                            sources.add("${"$"}pkg.${"$"}clsName")
                        }
                    }
                }
            }
        }
        
        // Generate SourceRegistry.kt
        val registryFile = File(megaAppSrcDir, "SourceRegistry.kt")
        val sb = java.lang.StringBuilder()
        sb.appendLine("package eu.kanade.tachiyomi.mega.generated")
        sb.appendLine()
        sb.appendLine("import eu.kanade.tachiyomi.source.SourceFactory")
        sb.appendLine("import eu.kanade.tachiyomi.source.Source")
        sb.appendLine()
        sb.appendLine("class SourceRegistry : SourceFactory {")
        sb.appendLine("    override fun createSources(): List<Source> {")
        sb.appendLine("        return listOf(")
        sources.forEach {
            if (it.startsWith("ExtensionGenerated")) {
                sb.appendLine("            $it(),")
            } else {
                sb.appendLine("            $it(),")
            }
        }
        sb.appendLine("        )")
        sb.appendLine("    }")
        sb.appendLine("}")
        
        registryFile.writeText(sb.toString())
        project.logger.lifecycle("MegaPlugin: Generated SourceRegistry.kt with ${sources.size} sources.")
    }
    
    private fun extractRegex(text: String, regex: Regex): String? {
        return regex.find(text)?.groupValues?.get(1)
    }
    
    private fun computeSourceId(name: String, lang: String, versionId: Int = 1): Long {
        val key = "\${name.lowercase()}/\$lang/\$versionId"
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        return (0..7).map { bytes[it].toLong() and 0xff }
            .reduce { acc, l -> (acc shl 8) or l } and Long.MAX_VALUE
    }
}
