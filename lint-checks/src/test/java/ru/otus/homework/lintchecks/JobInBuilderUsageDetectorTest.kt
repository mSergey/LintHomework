package ru.otus.homework.lintchecks


import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

class JobInBuilderUsageDetectorTest {

    private val lintTask = TestLintTask
        .lint()
        .allowMissingSdk()
        .issues(JobInBuilderUsageDetector.ISSUE)
    @Test
    fun `should detect job in builder usage`() {
        lintTask
            .files(
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.linthomework.jobinbuilderusage

                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Dispatchers
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.NonCancellable
                    import kotlinx.coroutines.SupervisorJob
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch

                    class JobInBuilderTestCase(private val job: Job) : ViewModel() {
                        fun case2() {
                            viewModelScope.launch(Job()) {
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                    """.trimIndent()
                )
            ).run()
            .expectClean()
    }

}