package eu.kanade.tachiyomi.megaplugin

import java.io.File

class MetadataCollector {
    
    fun parseSourceClass(file: File): String? {
        val tokens = tokenize(file.readText())
        var currentPackage = ""
        var hasSourceAnnotation = false
        
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "package" && i + 1 < tokens.size) {
                currentPackage = tokens[i + 1]
            } else if (token == "@Source") {
                hasSourceAnnotation = true
            } else if (token == "class" && hasSourceAnnotation && i + 1 < tokens.size) {
                return if (currentPackage.isNotEmpty()) {
                    "$currentPackage.${tokens[i + 1]}"
                } else {
                    tokens[i + 1]
                }
            }
            i++
        }
        return null
    }

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
                    while (i < text.length && text[i] != '"') {
                        if (text[i] == '\\') i++
                        i++
                    }
                    i++
                }
                text[i].isLetterOrDigit() || text[i] == '.' || text[i] == '@' || text[i] == '_' || text[i] == '*' -> {
                    val start = i
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '.' || text[i] == '@' || text[i] == '_' || text[i] == '*')) {
                        i++
                    }
                    tokens.add(text.substring(start, i))
                }
                else -> i++
            }
        }
        return tokens
    }
}
