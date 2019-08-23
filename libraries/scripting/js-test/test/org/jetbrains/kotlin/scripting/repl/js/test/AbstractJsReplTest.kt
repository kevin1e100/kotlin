/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.repl.js.test

import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.repl.ReplCodeLine
import org.jetbrains.kotlin.cli.common.repl.ReplCompileResult
import org.jetbrains.kotlin.cli.common.repl.ReplCompiler
import org.jetbrains.kotlin.cli.common.repl.ReplEvalResult
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.scripting.compiler.plugin.ScriptingCompilerConfigurationComponentRegistrar
import org.jetbrains.kotlin.scripting.configuration.ScriptingConfigurationKeys
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.platform
import org.jetbrains.kotlin.scripting.repl.js.JsReplEvaluator
import org.jetbrains.kotlin.scripting.repl.js.ReplMessageCollector
import java.io.Closeable
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.JsDependency

abstract class AbstractJsReplTest : Closeable {
    abstract fun createCompiler(): ReplCompiler
    abstract fun preprocessEvaluation()

    fun compile(codeLine: ReplCodeLine): ReplCompileResult {
        return compiler.compile(compiler.createState(), codeLine)
    }

    fun evaluate(compileResult: ReplCompileResult.CompiledClasses): ReplEvalResult {
        return jsEvaluator.eval(jsEvaluator.createState(), compileResult)
    }

    fun reset() {
        compiler = createCompiler()
        jsEvaluator = JsReplEvaluator()
        preprocessEvaluation()
    }

    private var collector: ReplMessageCollector = ReplMessageCollector()
    protected val disposable = Disposer.newDisposable()
    protected val environment = KotlinCoreEnvironment.createForProduction(
        disposable, loadConfiguration(), EnvironmentConfigFiles.JS_CONFIG_FILES
    )

    lateinit var compiler: ReplCompiler
    lateinit var jsEvaluator: JsReplEvaluator

    private var snippetId: Int = 1 //index 0 for klib
    fun newSnippetId(): Int = snippetId++

    private fun loadConfiguration(): CompilerConfiguration {
        val configuration = CompilerConfiguration()
        configuration.add(ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS, ScriptingCompilerConfigurationComponentRegistrar())
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, collector)
        configuration.put(CommonConfigurationKeys.MODULE_NAME, "repl.kts")
        val scriptConfiguration = ScriptCompilationConfiguration {
            baseClass("kotlin.Any")
            dependencies.append(JsDependency("compiler/ir/serialization.js/build/fullRuntime/klib"))
            platform.put("JS")
        }
        configuration.add(
            ScriptingConfigurationKeys.SCRIPT_DEFINITIONS,
            ScriptDefinition.FromConfigurations(ScriptingHostConfiguration(), scriptConfiguration, null)
        )
        return configuration
    }
}
