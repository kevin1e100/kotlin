/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal object CompilationFreeArgsValidator : AggregateReporter() {

    private const val EXTRA_PROPERTY_NAME = "org.jetbrains.kotlin.native.incorrectFreeArgs"

    private data class IncorrectArgumentsReport(val compilation: KotlinNativeCompilation, val incorrectArgs: List<String>) {
        val target: KotlinNativeTarget
            get() = compilation.target

        val project: Project
            get() = target.project
    }

    private fun String.startsWithIncorrectPrefix() = incorrectArgPrefixes.any { startsWith(it) }
    private fun String.disablesDevirtualization() = startsWith("-Xdisable-phases=") && contains("Devirtualization")

    private fun Project.getOrRegisterIncorrectArguments(): MutableList<IncorrectArgumentsReport> =
        getOrRegisterData(this, EXTRA_PROPERTY_NAME)

    fun validate(compilation: KotlinNativeCompilation) {
        val incorrectArgs = compilation.kotlinOptions.freeCompilerArgs.filter { arg ->
            arg.startsWithIncorrectPrefix() || arg.disablesDevirtualization()
        }
        if (incorrectArgs.isNotEmpty()) {
            compilation.target.project
                .getOrRegisterIncorrectArguments()
                .add(IncorrectArgumentsReport(compilation, incorrectArgs))
        }
    }

    override fun printWarning(project: Project) {
        // filterIsInstance helps against potential class loaders conflict or misconfiguration.
        @Suppress("UselessCallOnCollection")
        val incorrectArgs = project
            .getOrRegisterIncorrectArguments()
            .filterIsInstance<IncorrectArgumentsReport>()
            .sortedBy { "${it.project.path}.${it.target.name}.${it.compilation.name}" }

        if (incorrectArgs.isEmpty()) {
            return
        }

        val message = buildString {
            appendln()
            appendln("The following free compiler arguments must be specified for a binary instead of a compilation:")
            incorrectArgs.forEach {
                appendln(
                    """
                    ---
                    project '${it.project.path}'
                    compilation '${it.target.name}.${it.compilation.name}'
                    arguments [${it.incorrectArgs.joinToString()}]
                    """.trimIndent()
                )
            }
            appendln(
                """
                ---

                Please move them into final binary declarations. E.g. binaries.executable { freeCompilerArgs += "..." }
                See more about final binaries: https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#building-final-native-binaries.
                """.trimIndent()
            )
        }
        project.logger.warn(message)
    }

    private val incorrectArgPrefixes = listOf(
        // Optimizations/debug info.
        "-opt",
        "-g",
        "-ea",
        "-enable-assertions",
        // Test runners.
        "-trn",
        "-generate-no-exit-test-runner",
        "-tr",
        "-generate-test-runner",
        "-trw",
        "-generate-worker-test-runner",
        // Linker parameters and entry point.
        "-linker-option",
        "-linker-options",
        "-nl",
        "-native-library",
        "-e",
        "-entry",
        "-nomain",
        // Runtime settings.
        "-memory-model",

        // Coverage.
        "-Xcoverage",
        "-Xcoverage-file",
        "-Xlibrary-to-cover",
        // Reverse ObjC interop and framework producing.
        "-Xembed-bitcode",
        "-Xembed-bitcode-marker",
        "-Xexport-library",
        "-Xframework-import-header",
        "-Xobjc-generics",
        "-Xstatic-framework",
        // Advanced debug info.
        "-Xdebug-info-version",
        "-Xg0",
        // Other.
        "-Xinclude",
        "-Xruntime"
    )
}
