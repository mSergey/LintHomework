package ru.otus.homework.lintchecks


import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestMode
import org.junit.Before
import org.junit.Test

class JobInBuilderUsageDetectorTest {

    private lateinit var lintTask: TestLintTask

    @Before
    fun initLintTask() {
        lintTask = TestLintTask
            .lint()
            .allowMissingSdk()
            .issues(JobInBuilderUsageDetector.ISSUE)
    }

    @Test
    fun `should detect Job() call usage inside coroutine builder`() {
        lintTask
            .files(
                androidxLifecyclePackageStub,
                kotlinxCoroutinesPackageStub,
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.lintchecks
            
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Dispatchers
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.NonCancellable
                    import kotlinx.coroutines.SupervisorJob
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
            
                    class JobInBuilderTestCase(
                        private val job: Job
                    ) : ViewModel() {
                       
                        fun case2() {
                            viewModelScope.launch(Job()) {
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                    """.trimIndent()
                )
            )
            .run()
            .expect(
                """
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:17: Warning: Job instance should not be passed into coroutine builder. [JobInBuilderUsage]
        viewModelScope.launch(Job()) {
                              ~~~~~
0 errors, 1 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should detect SupervisorJob() call usage inside coroutine builder`() {
        lintTask
            .files(
                androidxLifecyclePackageStub,
                kotlinxCoroutinesPackageStub,
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.lintchecks
            
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Dispatchers
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.NonCancellable
                    import kotlinx.coroutines.SupervisorJob
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
            
                    class JobInBuilderTestCase(
                        private val job: Job
                    ) : ViewModel() {
                        
                        fun case1() {
                            viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
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
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:17: Warning: Job instance should not be passed into coroutine builder. [JobInBuilderUsage]
                            viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
                                                                   ~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should detect Job instance passing into coroutine builder`() {
        lintTask
            .files(
                androidxLifecyclePackageStub,
                kotlinxCoroutinesPackageStub,
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.lintchecks
            
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Dispatchers
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.NonCancellable
                    import kotlinx.coroutines.SupervisorJob
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
            
                    class JobInBuilderTestCase(
                        private val job: Job
                    ) : ViewModel() {
            
                        fun case3() {
                            viewModelScope.launch(job) {
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
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:17: Warning: Job instance should not be passed into coroutine builder. [JobInBuilderUsage]
                            viewModelScope.launch(job) {
                                                  ~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should detect child launch() call when NonCancellable passing into coroutine builder`() {
        lintTask
            .files(
                androidxLifecyclePackageStub,
                kotlinxCoroutinesPackageStub,
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.lintchecks
            
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Dispatchers
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.NonCancellable
                    import kotlinx.coroutines.SupervisorJob
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
            
                    class JobInBuilderTestCase(
                        private val job: Job
                    ) : ViewModel() {
                        
                        fun case4() {
                            viewModelScope.launch(NonCancellable) {
                                launch {  }
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                    """.trimIndent()
                )
            )
            .testModes(TestMode.REORDER_ARGUMENTS)
            .run()
            .expect(
                """
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:18: Warning: NonCancellable instance usage inside coroutine builder will brake an exception handling for all coroutines in the hierarchy. [JobInBuilderUsage]
                                launch {  }
                                ~~~~~~~~~~~
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:21: Warning: Job instance should not be passed into coroutine builder. [JobInBuilderUsage]
                            }, context = NonCancellable)
                                         ~~~~~~~~~~~~~~
                    0 errors, 2 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should detect NonCancellable passing into coroutine builder`() {
        lintTask
            .files(
                androidxLifecyclePackageStub,
                kotlinxCoroutinesPackageStub,
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.lintchecks
            
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Dispatchers
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.NonCancellable
                    import kotlinx.coroutines.SupervisorJob
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
            
                    class JobInBuilderTestCase(
                        private val job: Job
                    ) : ViewModel() {
                        
                        fun case5() {
                            viewModelScope.launch(NonCancellable) {
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                    """.trimIndent()
                )
            )
            .testModes(TestMode.REORDER_ARGUMENTS)
            .run()
            .expect(
                """
                    src/ru/otus/homework/lintchecks/JobInBuilderTestCase.kt:20: Warning: Job instance should not be passed into coroutine builder. [JobInBuilderUsage]
                            }, context = NonCancellable)
                                         ~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should not detect any incidents`() {
        lintTask
            .files(
                androidxLifecyclePackageStub,
                kotlinxCoroutinesPackageStub,
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.lintchecks
            
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.SupervisorJob
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
            
                    class JobInBuilderTestCase(
                        private val job: Job
                    ) : ViewModel() {
                        val supervisorJob = SupervisorJob()
                        fun case1() {
                            viewModelScope.launch() {
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
