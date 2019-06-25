package net.pototskiy.apps.lomout.api.config

import net.pototskiy.apps.lomout.api.config.resolver.FilesAndIvyResolver
import org.jetbrains.kotlin.script.util.DependsOn
import org.jetbrains.kotlin.script.util.Import
import org.jetbrains.kotlin.script.util.Repository
import java.io.File
import java.io.Serializable
import kotlin.script.dependencies.ScriptContents
import kotlin.script.experimental.api.ExternalSourceCode
import kotlin.script.experimental.api.RefineScriptCompilationConfigurationHandler
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptConfigurationRefinementContext
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.asDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.foundAnnotations
import kotlin.script.experimental.api.importScripts
import kotlin.script.experimental.jvm.updateClasspath

/**
 * Config script configuration on annotations
 */
@Suppress("ReturnCount", "TooGenericExceptionCaught")
class KtsConfigurator : RefineScriptCompilationConfigurationHandler, Serializable {
    /**
     * Main configuration function
     *
     * @param context The script context
     * @return ResultWithDiagnostics<ScriptCompilationConfiguration>
     */
    override operator fun invoke(
        context: ScriptConfigurationRefinementContext
    ): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val resolver = FilesAndIvyResolver()
        val logger = MainAndIdeLogger()

        val diagnostics = arrayListOf<ScriptDiagnostic>()

        val annotations = context.collectedData
            ?.get(ScriptCollectedData.foundAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

        val scriptLocation = (context.script as? ExternalSourceCode)?.externalLocation?.toURI()
        val scriptBaseDir = if (scriptLocation != null) File(scriptLocation).parentFile else null
        val importedSources = getImportScripts(annotations, scriptBaseDir)

        val resolvedClassPath = try {
            val scriptContents = object : ScriptContents {
                override val annotations: Iterable<Annotation> =
                    annotations.filter { it is DependsOn || it is Repository }
                override val file: File? = null
                override val text: CharSequence? = null
            }
            resolver.resolve(
                scriptContents,
                emptyMap(),
                { _, _, _ -> },
                null
            ).get()?.classpath?.toList()
        } catch (e: Throwable) {
            @Suppress("SpreadOperator")
            return ResultWithDiagnostics.Failure(
                *diagnostics.toTypedArray(),
                e.asDiagnostics(path = context.script.locationId)
            )
        }

        val newConf = ScriptCompilationConfiguration(context.compilationConfiguration) {
            if (resolvedClassPath != null && resolvedClassPath.isNotEmpty()) {
                updateClasspath(resolvedClassPath)
                logger.trace(
                    "Classpath updated with: ${resolvedClassPath.joinToString(",") { it.absolutePath }}"
                )
            }
            if (importedSources.isNotEmpty()) {
                importScripts.append(importedSources)
                logger.trace(
                    "${context.script.name} imports next scripts: " +
                            importedSources.joinToString(",") { it.name ?: "" }
                )
            }
        }
        return newConf.asSuccess(diagnostics)
    }

    private fun getImportScripts(
        annotations: List<Annotation>,
        scriptBaseDir: File?
    ): List<SerializableFileScriptSource> {
        return annotations.flatMap {
            (it as? Import)?.paths?.map { sourceName ->
                SerializableFileScriptSource(scriptBaseDir?.resolve(sourceName) ?: File(sourceName))
            } ?: emptyList()
        }
    }
}
