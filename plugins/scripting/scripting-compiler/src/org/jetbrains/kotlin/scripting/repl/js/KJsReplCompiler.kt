/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.repl.js

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.repl.*
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsIrLinker
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsMangler
import org.jetbrains.kotlin.ir.backend.js.utils.JsMainFunctionDetector
import org.jetbrains.kotlin.ir.backend.js.utils.NameTables
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.descriptors.IrBuiltIns
import org.jetbrains.kotlin.ir.util.ExternalDependenciesGenerator
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.scripting.resolve.ScriptLightVirtualFile
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.StringScriptSource

/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

class KJsReplCompiler(
    private val environment: KotlinCoreEnvironment
) : ReplCompiler {
    private val analyzerEngine = JsReplCodeAnalyzer(environment)
    private val symbolTable = SymbolTable()
    private val deserializer: JsIrLinker
    private val context: JsIrBackendContext
    private val irBuiltIns: IrBuiltIns
    private val namer: NameTables

    private val deserializedModuleFragments: List<IrModuleFragment>

    val stdlibCompiledResult: String

    init {
        val messageCollector = environment.configuration[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY] as MessageCollector

        setIdeaIoUseFallback()

        val snippet = ""
        val lineNumber = 0
        val codeLine = makeReplCodeLine(lineNumber, snippet)
        val sourceCode = StringScriptSource(snippet, "line-$lineNumber.kts")

        val snippetKtFile = getScriptKtFile(
            sourceCode,
            snippet,
            environment.project
        ).valueOrNull()
        require(snippetKtFile != null)

        analyzerEngine.analyzeReplLine(snippetKtFile, codeLine).also {
            AnalyzerWithCompilerReport.reportDiagnostics(it, messageCollector)
            require(!messageCollector.hasErrors())
        }

        val psi2ir = Psi2IrTranslator(environment.configuration.languageVersionSettings)
        val psi2irContext = psi2ir.createGeneratorContext(
            analyzerEngine.context.module,
            analyzerEngine.trace.bindingContext,
            symbolTable
        )

        irBuiltIns = IrBuiltIns(analyzerEngine.context.module.builtIns, psi2irContext.typeTranslator, symbolTable)
        deserializer = JsIrLinker(
            psi2irContext.moduleDescriptor,
            JsMangler,
            emptyLoggingContext,
            irBuiltIns,
            symbolTable
        ).also { it.isReplInitializing = true }

        deserializedModuleFragments = analyzerEngine.dependencies.map {
            deserializer.deserializeIrModuleHeader(analyzerEngine.modulesStructure.getModuleDescriptor(it))!!
        }

        val irModuleFragment = psi2irContext.generateModuleFragment(listOf(snippetKtFile), deserializer)

        environment.configuration.put(JSConfigurationKeys.MODULE_KIND, ModuleKind.PLAIN)

        context = JsIrBackendContext(
            irModuleFragment.descriptor,
            psi2irContext.irBuiltIns,
            psi2irContext.symbolTable,
            irModuleFragment,
            emptySet(),
            environment.configuration,
            true
        )

        val irFiles = sortDependencies(deserializedModuleFragments).flatMap { it.files } + irModuleFragment.files

        irModuleFragment.files.clear()
        irModuleFragment.files += irFiles

        ExternalDependenciesGenerator(
            moduleDescriptor = irModuleFragment.descriptor,
            symbolTable = psi2irContext.symbolTable,
            irBuiltIns = psi2irContext.irBuiltIns
        ).generateUnboundSymbolsAsDependencies()

        val pair = compileForRepl(
            context,
            null,
            irModuleFragment
        )

        namer = pair.first
        stdlibCompiledResult = pair.second

        deserializer.isReplInitializing = false
    }

    override fun createState(lock: ReentrantReadWriteLock): IReplStageState<*> {
        return JsState(lock)
    }

    override fun check(state: IReplStageState<*>, codeLine: ReplCodeLine): ReplCheckResult {
        return ReplCheckResult.Ok()
    }

    override fun compile(state: IReplStageState<*>, codeLine: ReplCodeLine): ReplCompileResult {
        val snippet = codeLine.code
        val snippetId = codeLine.no

        val messageCollector = environment.configuration[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY] as MessageCollector

        setIdeaIoUseFallback()

        val sourceCode = StringScriptSource(snippet, "line-$snippetId.kts")
        val snippetKtFile = getScriptKtFile(
            sourceCode,
            snippet,
            environment.project
        ).valueOr { return ReplCompileResult.Error(it.reports.joinToString { r -> r.message }) }

        analyzerEngine.analyzeReplLine(snippetKtFile, codeLine).also {
            AnalyzerWithCompilerReport.reportDiagnostics(it, messageCollector)
            if (messageCollector.hasErrors()) return ReplCompileResult.Error("Error while analysis")
        }

        val psi2ir = Psi2IrTranslator(environment.configuration.languageVersionSettings)
        val psi2irContext = psi2ir.createGeneratorContext(
            analyzerEngine.context.module,
            analyzerEngine.trace.bindingContext,
            symbolTable
        )

        val irModuleFragment = psi2irContext.generateModuleFragment(listOf(snippetKtFile), deserializer)

        irModuleFragment.files += context.implicitDeclarationFile
        context.implicitDeclarationFile.declarations.clear()

        environment.configuration.put(JSConfigurationKeys.MODULE_KIND, ModuleKind.PLAIN)

        val (_, code) = compileForRepl(
            context,
            JsMainFunctionDetector.getMainFunctionOrNull(irModuleFragment),
            irModuleFragment,
            namer
        )

        return createCompileResult(
            LineId(codeLine),
            code
        )
    }
}

class ReplMessageCollector : MessageCollector {
    private var hasErrors = false
    private var messages = mutableListOf<Pair<CompilerMessageSeverity, String>>()

    override fun clear() {
        hasErrors = false
        messages.clear()
    }

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {
        if (severity == CompilerMessageSeverity.ERROR) hasErrors = true
        messages.add(Pair(severity, message))
    }

    override fun hasErrors(): Boolean {
        return hasErrors
    }

    fun hasNotErrors(): Boolean {
        return !hasErrors
    }

    fun getMessage(): String {
        val resultMessage = StringBuilder("Found ${messages.size} problems:\n")
        for (m in messages) {
            resultMessage.append(m.first.toString() + " : " + m.second + "\n")
        }
        return resultMessage.toString()
    }
}

fun getScriptKtFile(
    script: SourceCode,
    scriptText: String,
    project: Project
): ResultWithDiagnostics<KtFile> {
    val psiFileFactory: PsiFileFactoryImpl = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
    val virtualFile = ScriptLightVirtualFile(
        script.name!!,
        (script as? FileBasedScriptSource)?.file?.path,
        scriptText
    )
    val ktFile = psiFileFactory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false) as KtFile?
    return when {
        ktFile == null -> ResultWithDiagnostics.Failure(
            ScriptDiagnostic(
                message = "Cannot create PSI",
                severity = ScriptDiagnostic.Severity.ERROR
            )
        )
        ktFile.declarations.firstIsInstanceOrNull<KtScript>() == null -> ResultWithDiagnostics.Failure(
            ScriptDiagnostic(
                message = "There is not Script",
                severity = ScriptDiagnostic.Severity.ERROR
            )
        )
        else -> ktFile.asSuccess()
    }
}

fun makeReplCodeLine(no: Int, code: String): ReplCodeLine = ReplCodeLine(no, 0, code)
