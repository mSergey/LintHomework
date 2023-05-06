package ru.otus.homework.lintchecks


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
                jobInBuilderTestFile,
                androidxLifecyclePackageStub,
                kotlinxCoroutinesPackageStub
            ).run()
            .expect(
                """
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:24: Warning: Job instance should not be passed into coroutine builder. [JobInBuilderUsage]
                                viewModelScope.launch(Job()) {
                                                      ~~~~~
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:31: Warning: Job instance should not be passed into coroutine builder. [JobInBuilderUsage]
                                viewModelScope.launch(job) {
                                                      ~~~
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:39: Warning: NonCancellable instance usage inside coroutine builder will brake an exception handling for all coroutines in the hierarchy. [JobInBuilderUsage]
                                    launch {  }
                                    ~~~~~~~~~~~
                    0 errors, 3 warnings
                """.trimIndent()
            )
    }

}