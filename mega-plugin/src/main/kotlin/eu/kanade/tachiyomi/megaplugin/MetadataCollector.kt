package eu.kanade.tachiyomi.megaplugin

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import java.io.File

class MetadataCollector {
    
    private val psiFactory: KtPsiFactory

    init {
        val config = CompilerConfiguration()
        config.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val project = KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(),
            config,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).project
        psiFactory = KtPsiFactory(project)
    }

    fun createKtFile(file: File): KtFile {
        return psiFactory.createFile(file.readText())
    }

    fun parseSourceClass(file: File): String? {
        val ktFile = createKtFile(file)
        var sourceClassFqName: String? = null

        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                super.visitClass(klass)
                val hasSource = klass.annotationEntries.any { it.shortName?.asString() == "Source" }
                if (hasSource) {
                    sourceClassFqName = klass.fqName?.asString()
                }
            }
        })
        
        return sourceClassFqName
    }
}
