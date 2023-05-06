package ru.otus.homework.lintchecks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

class GlobalScopeUsageDetectorTest {

    private val lintTask = TestLintTask
        .lint()
        .allowMissingSdk()
        .issues(GlobalScopeUsageDetector.ISSUE)

    @Test
    fun `should detect global scope usage`() {
        lintTask
            .files(
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.linthomework.globalscopeusage

                    class GlobalScopeTestCase(private val scope: CoroutineScope) : ViewModel() {
                        fun case1() {
                            GlobalScope.launch {
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                """.trimIndent()
                )
            ).run()
            .expect(
                """
                src/ru/otus/homework/linthomework/globalscopeusage/GlobalScopeTestCase.kt:5: Warning: GlobalScope should not use [GlobalScopeUsage]
                        GlobalScope.launch {
                        ~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should not detect global scope usage`() {
        lintTask
            .files(
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.linthomework.globalscopeusage

                    class GlobalScopeTestCase(private val scope: CoroutineScope) {
                        fun case1() {
                            scope.launch {
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                """.trimIndent()
                )
            ).run()
            .expect(
                """
                No warnings.
                """.trimIndent()
            )
    }
}