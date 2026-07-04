package eu.kanade.tachiyomi.megaplugin

import java.io.File

data class ExtensionMetadata(
    val pkg: String,
    val className: String,
    val isAbstract: Boolean,
    val name: String,
    val lang: String,
    val baseUrl: String,
    val isMultisrc: Boolean = false,
    val themeClass: String = ""
)

class MetadataCollector {
    
    fun parseImports(file: File): List<String> {
        val tokens = tokenize(file.readText())
        val imports = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == "import" && i + 1 < tokens.size) {
                imports.add(tokens[i + 1])
            }
            i++
        }
        return imports
    }

    private fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            when {
                text[i].isWhitespace() -> i++
                text.startsWith("//", i) -> {
                    while (i < text.length && text[i] != '\n') i++
                }
                text.startsWith("/*", i) -> {
                    while (i < text.length && !text.startsWith("*/", i)) i++
                    i += 2
                }
                text[i] == '"' -> {
                    i++
                    var str = ""
                    while (i < text.length && text[i] != '"') {
                        if (text[i] == '\\') i++
                        str += text[i]
                        i++
                    }
                    i++
                    tokens.add("\"\$str\"")
                }
                text[i].isLetterOrDigit() || text[i] == '.' || text[i] == '@' || text[i] == '_' || text[i] == '*' -> {
                    val start = i
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '.' || text[i] == '@' || text[i] == '_' || text[i] == '*')) {
                        i++
                    }
                    tokens.add(text.substring(start, i))
                }
                text[i] == '=' || text[i] == '{' || text[i] == '}' -> {
                    tokens.add(text[i].toString())
                    i++
                }
                else -> i++
            }
        }
        return tokens
    }
}
